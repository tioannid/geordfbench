const express = require("express");
const fs = require("fs");
const path = require("path");

var router = express.Router();

const specCategory = "hosts";
const specEntity = "Host";
const targetDir = path.join(json_lib_path, specCategory);
console.log(`initializing ${specCategory}...`);

function getFiles(folderName) {
  const storeFiles = fs.readdirSync(folderName);
  console.log(storeFiles);
  return storeFiles;
}

// middleware for checking the CRUD settings
function checkCRUD(req, res, next) {
  // retrieve the CRUD settings for the specification category
  let crud = res.app.locals.endpointConfig.CRUD_MAP[specCategory];
  res.app.locals.canCreate = crud.charAt(0) === "C";
  res.app.locals.canRead = crud.charAt(1) === "R";
  res.app.locals.canUpdate = crud.charAt(2) === "U";
  res.app.locals.canDelete = crud.charAt(3) === "D";
  next();
}

// add checkCRUD middleware for all router paths
router.use(checkCRUD);

// get a list of host specifications
router.get("/", (req, res) => {
  if (res.app.locals.canRead) {
    const storeFiles = getFiles(targetDir);
    return res.json(storeFiles);
  } else {
    return res
      .status(404)
      .json(
        `${specEntity} read functionality is disabled by the endpoint's configuration!`
      );
  }
});

// Retrieve a host specification
router.get("/:spec", (req, res) => {
  if (res.app.locals.canRead) {
    const spec = req.params.spec;
    // check if spec is a filename with .json file type
    const specFullPathName =
      path.extname(spec) !== ".json"
        ? path.join(targetDir, spec + ".json")
        : path.join(targetDir, spec);
    console.log(specFullPathName);
    if (!fs.existsSync(specFullPathName)) {
      return res.status(404).json({
        error: `Host specification ${specFullPathName} does not exist!`,
      });
    }
    const result = JSON.parse(fs.readFileSync(specFullPathName));
    console.log(result);
    return res.json(result);
  } else {
    return res
      .status(404)
      .json(
        `${specEntity} read functionality is disabled by the endpoint's configuration!`
      );
  }
});

// Send a host specification to a JSON file
router.post("/:newspec", (req, res) => {
  if (res.app.locals.canCreate) {
    // declare an example in order to augment error messages when necessary
    const exampleJSON = JSON.parse(
      '{"classname":"gr.uoa.di.rdf.geordfbench.runtime.hosts.impl.SimpleHost",  "name":"NUC8i7BEH", "ipAddr":"192.168.1.44", "ram":32, "os": {"classname":"gr.uoa.di.rdf.geordfbench.runtime.os.impl.UbuntuJammyOS", "name":"Ubuntu-jammy", "shell_cmd":"/bin/sh", "sync_cmd":"sync", "clearcache_cmd":"sudo /sbin/sysctl vm.drop_caches=3"}, "sourceFileDir":"/data/Geographica2_Datasets", "reposBaseDir":"/data", "reportsBaseDir":"/data/Results_Store"}'
    );
    const spec = req.params.newspec;
    // check if spec is a filename with .json file type
    const specFullPathName =
      path.extname(spec) !== ".json"
        ? path.join(targetDir, spec + ".json")
        : path.join(targetDir, spec);
    console.log(
      `Will be creating a host specification file in the path ${specFullPathName}`
    );
    if (fs.existsSync(specFullPathName)) {
      return res
        .status(404)
        .json({ error: `${specFullPathName} already exists!` });
    }

    // grap the POST body
    const body = req.body;
    // validate required values
    if (
      !(
        body.hasOwnProperty("classname") &&
        body.hasOwnProperty("name") &&
        body.hasOwnProperty("ipAddr") &&
        body.hasOwnProperty("ram") &&
        body.hasOwnProperty("os") &&
        body.hasOwnProperty("sourceFileDir") &&
        body.hasOwnProperty("reposBaseDir") &&
        body.hasOwnProperty("reportsBaseDir")
      )
    ) {
      return res.status(400).json({
        error: "Fields are missing! Please check the following example:",
        example: exampleJSON,
      });
    }
    // check if noQueryResultToReport is integer
    let ram = body.ram;
    if (!Number.isInteger(ram)) {
      return res.status(400).json({ error: "ram value is not integer!" });
    }
    fs.writeFileSync(specFullPathName, JSON.stringify(body, null, 2));
    const result = JSON.parse(fs.readFileSync(specFullPathName));
    return res.status(201).json(result);
  } else {
    return res
      .status(404)
      .json(
        `${specEntity} create functionality is disabled by the endpoint's configuration!`
      );
  }
});

// Delete an existing host specification by name
router.delete("/:existingspec", (req, res) => {
  if (res.app.locals.canDelete) {
    const spec = req.params.existingspec;
    // check if spec is a filename with .json file type
    const specFullPathName =
      path.extname(spec) !== ".json"
        ? path.join(targetDir, spec + ".json")
        : path.join(targetDir, spec);
    console.log(
      `Will be deleting a host specification file in the path ${specFullPathName}`
    );
    if (!fs.existsSync(specFullPathName)) {
      return res
        .status(404)
        .json({ error: `${specFullPathName} does not exist!` });
    } else {
      fs.unlinkSync(specFullPathName, (err) => {
        if (err) {
          return res.status(404).json({
            error: `Could not delete ${specFullPathName}! Error is: ${err}`,
          });
        }
      });
      return res
        .status(204)
        .json({ success: `Deleted file: ${specFullPathName}` });
    }
  } else {
    return res
      .status(404)
      .json(
        `${specEntity} delete functionality is disabled by the endpoint's configuration!`
      );
  }
});

module.exports = router;
