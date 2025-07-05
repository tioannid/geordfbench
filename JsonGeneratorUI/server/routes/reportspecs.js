const express = require("express");
const fs = require("fs");
const path = require("path");

var router = express.Router();

const targetDir = path.join(json_lib_path, "reportspecs");

function getFiles(folderName) {
  const storeFiles = fs.readdirSync(folderName);
  console.log(storeFiles);
  return storeFiles;
}

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

// // Send a report specification to a JSON file
// router.post("/:fname", (req, res) => {
//   const spec = req.params.spec;
//   // check if spec is a filename with .json file type
//   const specFullPathName = (path.extname(spec) !== '.json') ? path.join(targetDir, spec + ".json") : path.join(targetDir, spec);
//   // console.log(specFullPathName);
//   if (!fs.existsSync(specFullPathName)) {
//     return res.status(404).json({ error: `${specFullPathName} already exists!` });
//   };

//   // grap the POST body
//   const body = req.body;
//   // validate required values
//   let noQueryResultToReport = body.noQueryResultToReport;
//   if (!body.noQueryResultToReport) {
//     return res.status(400).json({error: `noQueryResultToReport is missing!`});
//   }
//   // check if noQueryResultToReport is integer

//   if ()
//     const result = JSON.parse(fs.readFileSync(specFullPathName));
//   return res.json(result);
// });

module.exports = router;
