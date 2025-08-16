const express = require("express");
const fs = require("fs");
const path = require("path");

var router = express.Router();

const specCategory = "executionspecs";
const specEntity = "Execution spec";
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

// get a list of execution specifications
router.get("/", (req, res) => {
  if (res.app.locals.canCreate) {
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

// Retrieve an execution specification
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

// Send a execution specification to a JSON file
router.post("/:newspec", (req, res) => {
  if (res.app.locals.canCreate) {
    const spec = req.params.newspec;
    // check if spec is a filename with .json file type
    const specFullPathName =
      path.extname(spec) !== ".json"
        ? path.join(targetDir, spec + ".json")
        : path.join(targetDir, spec);
    console.log(
      `Will be creating an execution specification file in the path ${specFullPathName}`
    );
    if (fs.existsSync(specFullPathName)) {
      return res
        .status(404)
        .json({ error: `${specFullPathName} already exists!` });
    }

    // grap the POST body
    const body = req.body;
    // validate required values
    // declare an example in order to augment error messages when necessary
    let exampleJSON = JSON.parse(
      `{
  "classname" : "gr.uoa.di.rdf.geordfbench.runtime.executionspecs.impl.SimpleES",
  "execTypeReps" : {
    "COLD" : 3,
    "WARM" : 3
  },
  "maxDurationSecsPerQueryRep" : 86400,
  "maxDurationSecs" : 604800,
  "action" : "RUN",
  "avgFunc" : "QUERY_MEDIAN",
  "onColdFailure" : "SKIP_REMAINING_ALL_QUERY_EXECUTIONS",
  "clearCacheDelaymSecs" : 5000
}`
    );
    if (
      !(
        body.hasOwnProperty("classname") &&
        body.hasOwnProperty("execTypeReps") &&
        body.hasOwnProperty("maxDurationSecsPerQueryRep") &&
        body.hasOwnProperty("maxDurationSecs") &&
        body.hasOwnProperty("action") &&
        body.hasOwnProperty("avgFunc") &&
        body.hasOwnProperty("onColdFailure") &&
        body.hasOwnProperty("clearCacheDelaymSecs")
      )
    ) {
      return res.status(400).json({
        error: "Fields are missing! Please check the following example:",
        example: exampleJSON,
      });
    }
    // check if noQueryResultToReport is integer
    let maxDurationSecsPerQueryRep = body.maxDurationSecsPerQueryRep;
    if (!Number.isInteger(maxDurationSecsPerQueryRep)) {
      return res
        .status(403)
        .json({ error: "maxDurationSecsPerQueryRep value is not integer!" });
    }
    let maxDurationSecs = body.maxDurationSecs;
    if (!Number.isInteger(maxDurationSecs)) {
      return res
        .status(403)
        .json({ error: "maxDurationSecs value is not integer!" });
    }
    let clearCacheDelaymSecs = body.clearCacheDelaymSecs;
    if (!Number.isInteger(clearCacheDelaymSecs)) {
      return res
        .status(403)
        .json({ error: "clearCacheDelaymSecs value is not integer!" });
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

// Update an existing execution specification by name
router.put("/:existingspec", (req, res) => {
  if (res.app.locals.canUpdate) {
    const spec = req.params.existingspec;
    const specFullPathName =
      path.extname(spec) !== ".json"
        ? path.join(targetDir, spec + ".json")
        : path.join(targetDir, spec);
    
    if (!fs.existsSync(specFullPathName)) {
      return res.status(404).json({ error: `${specFullPathName} does not exist!` });
    }

    const body = req.body;
    // Basic validation for execution specs
    if (!body.hasOwnProperty("classname") || !body.hasOwnProperty("execTypeReps")) {
      return res.status(400).json({ error: "Required execution specification fields are missing!" });
    }

    fs.writeFileSync(specFullPathName, JSON.stringify(body, null, 2));
    const result = JSON.parse(fs.readFileSync(specFullPathName));
    return res.status(200).json(result);
  } else {
    return res.status(404).json(`${specEntity} update functionality is disabled!`);
  }
});

// Delete an existing execution specification by name
router.delete("/:existingspec", (req, res) => {
  if (res.app.locals.canDelete) {
    const spec = req.params.existingspec;
    // check if spec is a filename with .json file type
    const specFullPathName =
      path.extname(spec) !== ".json"
        ? path.join(targetDir, spec + ".json")
        : path.join(targetDir, spec);
    console.log(
      `Will be deleting a execution specification file in the path ${specFullPathName}`
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
