const express = require("express");
const fs = require("fs");
const path = require("path");
var router = express.Router();

const specCategory = "reportspecs";
const specEntity = "Report spec";
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

// get a list of report specifications
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

// Retrieve a report specification
router.get("/:spec", (req, res) => {
  if (res.app.locals.canRead) {
    const spec = req.params.spec;
    // check if spec is a filename with .json file type
    const specFullPathName =
      path.extname(spec) !== ".json"
        ? path.join(targetDir, spec + ".json")
        : path.join(targetDir, spec);
    // console.log(specFullPathName);
    if (!fs.existsSync(specFullPathName)) {
      return res
        .status(404)
        .json({ error: `${specFullPathName} does not exist!` });
    }
    const result = JSON.parse(fs.readFileSync(specFullPathName));
    return res.json(result);
  } else {
    return res
      .status(404)
      .json(
        `${specEntity} read functionality is disabled by the endpoint's configuration!`
      );
  }
});

// Send a report specification to a JSON file
router.post("/:newspec", (req, res) => {
  if (res.app.locals.canCreate) {
    const spec = req.params.newspec;
    // check if spec is a filename with .json file type
    const specFullPathName =
      path.extname(spec) !== ".json"
        ? path.join(targetDir, spec + ".json")
        : path.join(targetDir, spec);
    console.log(
      `Will be creating a report specification file in the path ${specFullPathName}`
    );
    if (fs.existsSync(specFullPathName)) {
      return res
        .status(404)
        .json({ error: `${specFullPathName} already exists!` });
    }

    // grap the POST body
    const body = req.body;
    // validate required values
    if (!body.hasOwnProperty("noQueryResultToReport")) {
      return res
        .status(400)
        .json({ error: "noQueryResultToReport is missing!" });
    }
    // check if noQueryResultToReport is integer
    let noQueryResultToReport = body.noQueryResultToReport;
    if (!Number.isInteger(noQueryResultToReport)) {
      return res
        .status(400)
        .json({ error: "noQueryResultToReport value is not integer!" });
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

// Update an existing report specification by name
router.put("/:existingspec", (req, res) => {
  if (res.app.locals.canUpdate) {
    const spec = req.params.existingspec;
    // check if spec is a filename with .json file type
    const specFullPathName =
      path.extname(spec) !== ".json"
        ? path.join(targetDir, spec + ".json")
        : path.join(targetDir, spec);
    console.log(
      `Will be updating a report specification file in the path ${specFullPathName}`
    );
    if (!fs.existsSync(specFullPathName)) {
      return res
        .status(404)
        .json({ error: `${specFullPathName} does not exist!` });
    }

    // grab the PUT body
    const body = req.body;
    // validate required values
    if (!body.hasOwnProperty("noQueryResultToReport")) {
      return res
        .status(400)
        .json({ error: "noQueryResultToReport is missing!" });
    }
    // check if noQueryResultToReport is integer
    let noQueryResultToReport = body.noQueryResultToReport;
    if (!Number.isInteger(noQueryResultToReport)) {
      return res
        .status(400)
        .json({ error: "noQueryResultToReport value is not integer!" });
    }
    fs.writeFileSync(specFullPathName, JSON.stringify(body, null, 2));
    const result = JSON.parse(fs.readFileSync(specFullPathName));
    return res.status(200).json(result);
  } else {
    return res
      .status(404)
      .json(
        `${specEntity} update functionality is disabled by the endpoint's configuration!`
      );
  }
});

// Delete an existing report specification by name
router.delete("/:existingspec", (req, res) => {
  if (res.app.locals.canDelete) {
    const spec = req.params.existingspec;
    // check if spec is a filename with .json file type
    const specFullPathName =
      path.extname(spec) !== ".json"
        ? path.join(targetDir, spec + ".json")
        : path.join(targetDir, spec);
    console.log(
      `Will be deleting a report specification file in the path ${specFullPathName}`
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
