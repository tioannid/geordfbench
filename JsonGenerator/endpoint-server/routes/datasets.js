const express = require("express");
const fs = require("fs");
const path = require("path");

var router = express.Router();

const specCategory = "datasets";
const specEntity = "Dataset";
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

// get a list of dataset specifications
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

// Retrieve a dataset specification
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

// Send a dataset specification to a JSON file
router.post("/:newspec", (req, res) => {
  if (res.app.locals.canCreate) {
    const spec = req.params.newspec;
    // check if spec is a filename with .json file type
    const specFullPathName =
      path.extname(spec) !== ".json"
        ? path.join(targetDir, spec + ".json")
        : path.join(targetDir, spec);
    console.log(
      `Will be creating a dataset specification file in the path ${specFullPathName}`
    );
    if (fs.existsSync(specFullPathName)) {
      return res
        .status(404)
        .json({ error: `${specFullPathName} already exists!` });
    }

    // grap the POST body
    const body = req.body;
    // declare an example in order to augment error messages when necessary
    let exampleDatasetJSON = JSON.parse(
      `{
  "classname": "gr.uoa.di.rdf.geordfbench.runtime.datasets.complex.impl.GeographicaDS",
  "name": "lubm-1_0",
  "relativeBaseDir": "LUBM",
  "simpleGeospatialDataSetList": [
    {
      "name": "lubm-1_0",
      "relativeBaseDir": "1_0",
      "dataFile": "lubm-1_0.nt",
      "rdfFormat": "N-TRIPLES",
      "mapUsefulNamespacePrefixes": {
        "geo": "<http://www.opengis.net/ont/geosparql#>",
        "rdf": "<http://www.w3.org/1999/02/22-rdf-syntax-ns#>",
        "owl": "<http://www.w3.org/2002/07/owl#>",
        "geof": "<http://www.opengis.net/def/function/geosparql/>",
        "xsd": "<http://www.w3.org/2001/XMLSchema#>",
        "rdfs": "<http://www.w3.org/2000/01/rdf-schema#>",
        "geo-sf": "<http://www.opengis.net/ont/sf#>"
      }
    }
  ],
  "mapDataSetContexts": {
    "lubm-1_0": ""
  },
  "n": 1
}`
    );
    if (
      !(
        body.hasOwnProperty("classname") &&
        body.hasOwnProperty("name") &&
        body.hasOwnProperty("relativeBaseDir") &&
        body.hasOwnProperty("simpleGeospatialDataSetList") &&
        body.hasOwnProperty("mapDataSetContexts") &&
        body.hasOwnProperty("n")
      )
    ) {
      return res.status(400).json({
        error:
          "Dataset fields are missing! Please check the following example:",
        example: exampleDatasetJSON,
      });
    }
    // check if n is integer
    let n = body.n;
    if (!Number.isInteger(n)) {
      return res.status(400).json({ error: "n value is not integer!" });
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

// Delete an existing dataset specification by name
router.delete("/:existingspec", (req, res) => {
  if (res.app.locals.canDelete) {
    const spec = req.params.existingspec;
    // check if spec is a filename with .json file type
    const specFullPathName =
      path.extname(spec) !== ".json"
        ? path.join(targetDir, spec + ".json")
        : path.join(targetDir, spec);
    console.log(
      `Will be deleting a dataset specification file in the path ${specFullPathName}`
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
