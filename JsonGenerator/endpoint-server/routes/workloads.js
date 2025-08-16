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

// 🔥 BEAST MODE: Create a new workload specification
router.post("/:newspec", (req, res) => {
  if (res.app.locals.canCreate) {
    const spec = req.params.newspec;
    const specFullPathName =
      path.extname(spec) !== ".json"
        ? path.join(targetDir, spec + ".json")
        : path.join(targetDir, spec);

    console.log(
      `Will be creating a workload specification file in the path ${specFullPathName}`
    );

    if (fs.existsSync(specFullPathName)) {
      return res.status(409).json({ error: `${specFullPathName} already exists!` });
    }

    const body = req.body;
    
    // Basic validation for workload specs
    if (!body.hasOwnProperty("classname") || !body.hasOwnProperty("dataset") || 
        !body.hasOwnProperty("queryset") || !body.hasOwnProperty("executionSpec")) {
      return res.status(400).json({ 
        error: "Required workload specification fields are missing! Must include 'classname', 'dataset', 'queryset', and 'executionSpec'." 
      });
    }

    try {
      fs.writeFileSync(specFullPathName, JSON.stringify(body, null, 2));
      const result = JSON.parse(fs.readFileSync(specFullPathName));
      return res.status(201).json(result);
    } catch (error) {
      return res.status(500).json({ 
        error: `Failed to create workload specification: ${error.message}` 
      });
    }
  } else {
    return res.status(404).json(
      `${specEntity} create functionality is disabled by the endpoint's configuration!`
    );
  }
});

// 🔥 BEAST MODE: Update an existing workload specification by name
router.put("/:existingspec", (req, res) => {
  if (res.app.locals.canUpdate) {
    const spec = req.params.existingspec;
    const specFullPathName =
      path.extname(spec) !== ".json"
        ? path.join(targetDir, spec + ".json")
        : path.join(targetDir, spec);

    console.log(
      `Will be updating a workload specification file in the path ${specFullPathName}`
    );

    if (!fs.existsSync(specFullPathName)) {
      return res.status(404).json({ error: `${specFullPathName} does not exist!` });
    }

    const body = req.body;
    
    // Basic validation for workload specs
    if (!body.hasOwnProperty("classname") || !body.hasOwnProperty("dataset") || 
        !body.hasOwnProperty("queryset") || !body.hasOwnProperty("executionSpec")) {
      return res.status(400).json({ 
        error: "Required workload specification fields are missing! Must include 'classname', 'dataset', 'queryset', and 'executionSpec'." 
      });
    }

    try {
      fs.writeFileSync(specFullPathName, JSON.stringify(body, null, 2));
      const result = JSON.parse(fs.readFileSync(specFullPathName));
      return res.status(200).json(result);
    } catch (error) {
      return res.status(500).json({ 
        error: `Failed to update workload specification: ${error.message}` 
      });
    }
  } else {
    return res.status(404).json(
      `${specEntity} update functionality is disabled by the endpoint's configuration!`
    );
  }
});

// 🔥 BEAST MODE: Delete an existing workload specification by name
router.delete("/:existingspec", (req, res) => {
  if (res.app.locals.canDelete) {
    const spec = req.params.existingspec;
    const specFullPathName =
      path.extname(spec) !== ".json"
        ? path.join(targetDir, spec + ".json")
        : path.join(targetDir, spec);

    console.log(
      `Will be deleting a workload specification file in the path ${specFullPathName}`
    );

    if (!fs.existsSync(specFullPathName)) {
      return res.status(404).json({ error: `${specFullPathName} does not exist!` });
    }

    try {
      fs.unlinkSync(specFullPathName);
      return res.status(204).json({ success: `Deleted file: ${specFullPathName}` });
    } catch (error) {
      return res.status(500).json({
        error: `Could not delete ${specFullPathName}! Error is: ${error.message}`,
      });
    }
  } else {
    return res.status(404).json(
      `${specEntity} delete functionality is disabled by the endpoint's configuration!`
    );
  }
});

module.exports = router;
