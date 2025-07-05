const express = require("express");
const fs = require("fs");
const path = require("path");

var router = express.Router();

const targetDir = path.join(json_lib_path, "datasets");

function getFiles(folderName) {
  const storeFiles = fs.readdirSync(folderName);
  console.log(storeFiles);
  return storeFiles;
}

// get a list of dataset specifications
router.get("/", (req, res) => {
  const storeFiles = getFiles(targetDir);
  return res.json(storeFiles);
});

// Retrieve a dataset specification
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

module.exports = router;
