const express = require("express");
const fs = require("fs");
const path = require("path");

var router = express.Router();

const specCategory = "hosts";
const specEntity = "Host";
const targetDir = path.join(json_lib_path, specCategory);
console.log(`initializing ${specCategory}...`);

// retrieve the CRUD settings for the 'hosts' specification category
const crud = crud_map.hosts;
const canCreate = crud.charAt(0) === "C";
const canRead = crud.charAt(1) === "R";
const canUpdate = crud.charAt(2) === "U";
const canDelete = crud.charAt(3) === "D";

function getFiles(folderName) {
  const storeFiles = fs.readdirSync(folderName);
  console.log(storeFiles);
  return storeFiles;
}

if (canRead) {
  // get a list of host specifications
  router.get("/", (req, res) => {
    const storeFiles = getFiles(targetDir);
    return res.json(storeFiles);
  });

  // Retrieve a host specification
  router.get("/:spec", (req, res) => {
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
  });
} else {
  router.get("/", (req, res) => {
    return res
      .status(404)
      .json(
        `${specEntity} read functionality is disabled by the endpoint's configuration!`
      );
  });
  router.get("/:spec", (req, res) => {
    return res
      .status(404)
      .json(
        `${specEntity} read functionality is disabled by the endpoint's configuration!`
      );
  });
}

if (canCreate) {
  // declare an example in order to augment error messages when necessary
  let exampleJSON = JSON.parse(
    '{"classname":"gr.uoa.di.rdf.geordfbench.runtime.hosts.impl.SimpleHost",  "name":"NUC8i7BEH", "ipAddr":"192.168.1.44", "ram":32, "os": {"classname":"gr.uoa.di.rdf.geordfbench.runtime.os.impl.UbuntuJammyOS", "name":"Ubuntu-jammy", "shell_cmd":"/bin/sh", "sync_cmd":"sync", "clearcache_cmd":"sudo /sbin/sysctl vm.drop_caches=3"}, "sourceFileDir":"/data/Geographica2_Datasets", "reposBaseDir":"/data", "reportsBaseDir":"/data/Results_Store"}'
  );

  // Send a host specification to a JSON file
  router.post("/:newspec", (req, res) => {
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
  });
} else {
  router.post("/:newspec", (req, res) => {
    return res
      .status(404)
      .json(
        `${specEntity} create functionality is disabled by the endpoint's configuration!`
      );
  });
}

if (canDelete) {
  // Delete an existing host specification by name
  router.delete("/:existingspec", (req, res) => {
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
  });
} else {
  router.delete("/:existingspec", (req, res) => {
    return res
      .status(404)
      .json(
        `${specEntity} delete functionality is disabled by the endpoint's configuration!`
      );
  });
}

module.exports = router;
