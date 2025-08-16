const express = require("express");
const fs = require("fs");
const path = require("path");

var router = express.Router();

const specCategory = "workloads";
const specEntity = "Workload";
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

// get a list of workloads
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

// Retrieve a workload specification
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
      return res
        .status(404)
        .json({ error: `${specFullPathName} does not exist!` });
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

module.exports = router;
