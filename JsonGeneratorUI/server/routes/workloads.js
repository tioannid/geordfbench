const express = require("express");
const fs = require("fs");
const path = require("path");

var router = express.Router();

const specCategory = "workloads";
const specEntity = "Workload";
const targetDir = path.join(json_lib_path, specCategory);
console.log(`initializing ${specCategory}...`);

// retrieve the CRUD settings for the 'workloads' specification category
const crud = crud_map.workloads;
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
  // get a list of workloads
  router.get("/", (req, res) => {
    const storeFiles = getFiles(targetDir);
    return res.json(storeFiles);
  });

  // Retrieve a workload specification
  router.get("/:spec", (req, res) => {
    const spec = req.params.spec;
    // check if spec is a filename with .json file type
    const specFullPathName =
      path.extname(spec) !== ".json"
        ? path.join(targetDir, spec + ".json")
        : path.join(targetDir, spec);
    console.log(specFullPathName);
    if (!fs.existsSync(specFullPathName)) {
      return res
        .status(404)
        .json({ error: `${specFullPathName} does not exist!` });
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

module.exports = router;
