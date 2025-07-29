const express = require("express");
const fs = require("fs");
const path = require("path");
var router = express.Router();

const specCategory = "reportspecs";
const specEntity = "Report spec";
const targetDir = path.join(json_lib_path, specCategory);
console.log(`initializing ${specCategory}...`);

// retrieve the CRUD settings for the 'reportspecs' specification category
const crud = crud_map.reportspecs;
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
  // get a list of report specifications
  router.get("/", (req, res) => {
    const storeFiles = getFiles(targetDir);
    return res.json(storeFiles);
  });

  // Retrieve a report specification
  router.get("/:spec", (req, res) => {
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
  // Send a report specification to a JSON file
  router.post("/:newspec", (req, res) => {
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
  // Delete an existing report specification by name
  router.delete("/:existingspec", (req, res) => {
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
