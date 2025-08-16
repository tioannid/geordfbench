var express = require("express");
var router = express.Router();
const fs = require("fs");

const targetDir = json_lib_path;

function getFiles(folderName) {
  const storeFiles = fs.readdirSync(folderName);
  console.log(storeFiles);
  return storeFiles;
}

router.use("/config", require("./serverforms/config"));

// get a list of host specifications
router.get("/", (req, res) => {
  res.render("index", { title: "Hey", message: "Hello there!" });
});

module.exports = router;
