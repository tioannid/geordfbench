var express = require("express");
var router = express.Router();
const fs = require("fs");

const targetDir = json_lib_path;

function getFiles(folderName) {
  const storeFiles = fs.readdirSync(folderName);
  console.log(storeFiles);
  return storeFiles;
}

// get a list of host specifications
router.get("/", (req, res) => {
  const storeFiles = getFiles(targetDir);
  return res.json(storeFiles);
});


module.exports = router;