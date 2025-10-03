var express = require("express");
var router = express.Router();
const fs = require("fs");

const targetDir = json_lib_path;

function getFiles(folderName) {
  const storeFiles = fs.readdirSync(folderName);
  console.log(storeFiles);
  return storeFiles;
}

router.use("/config", require("./admin/config"));
router.use("/users", require("./admin/users"));

// get a list of host specifications
router.get("/", (req, res) => {
  console.log("The /admin path of the endpoint");
});

module.exports = router;
