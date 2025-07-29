const express = require("express");
const fs = require("fs");
const path = require("path");

var router = express.Router();

const specCategory = "reportsources";
const specEntity = "Report source";
const targetDir = path.join(json_lib_path, specCategory);
console.log(`initializing ${specCategory}...`);

// retrieve the CRUD settings for the 'reportsources' specification category
const crud = crud_map.reportsources;
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
  // get a list of report source specifications
  router.get("/", (req, res) => {
    const storeFiles = getFiles(targetDir);
    return res.json(storeFiles);
  });

  // Retrieve a report source specification
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

if (canCreate) {
  // Send a report source specification to a JSON file
  router.post("/:newspec", (req, res) => {
    const spec = req.params.newspec;
    // check if spec is a filename with .json file type
    const specFullPathName =
      path.extname(spec) !== ".json"
        ? path.join(targetDir, spec + ".json")
        : path.join(targetDir, spec);
    console.log(
      `Will be creating a report source specification file in the path ${specFullPathName}`
    );
    if (fs.existsSync(specFullPathName)) {
      return res
        .status(404)
        .json({ error: `${specFullPathName} already exists!` });
    }

    // grap the POST body
    const body = req.body;
    // validate #/components/schemas/BaseReportSource required values
    // declare an example in order to augment error messages when necessary
    let exampleBaseReportSourceJSON = JSON.parse(
      `{
  "classname": "gr.uoa.di.rdf.geordfbench.runtime.reportsource.impl.PostgreSQLRepSrc",
    "driver": "postgresql",
    "database": "geordfbench",
    "user": "geordfbench",
    "password": "geordfbench"
}`
    );
    if (
      !(
        body.hasOwnProperty("classname") &&
        body.hasOwnProperty("driver") &&
        body.hasOwnProperty("database") &&
        body.hasOwnProperty("user") &&
        body.hasOwnProperty("password")
      )
    ) {
      return res.status(400).json({
        error:
          "Base Report Source fields are missing! Please check the following example:",
        example: exampleBaseReportSourceJSON,
      });
    }

    // validate #/components/schemas/JDBCReportSource required values
    // declare an example in order to augment error messages when necessary
    let exampleJDBCReportSourceJSON = JSON.parse(
      `{
  "classname": "gr.uoa.di.rdf.geordfbench.runtime.reportsource.impl.PostgreSQLRepSrc",
  "driver": "postgresql",
  "hostname": "192.168.1.44",
  "althostname": "localhost",
  "port": 5432,
  "database": "geordfbench",
  "user": "geordfbench",
  "password": "geordfbench"
}`
    );
    if (
      body.classname ===
        "gr.uoa.di.rdf.geordfbench.runtime.reportsource.impl.PostgreSQLRepSrc" &&
      !(
        body.hasOwnProperty("hostname") &&
        body.hasOwnProperty("althostname") &&
        body.hasOwnProperty("port")
      )
    ) {
      return res.status(403).json({
        error:
          "JDBC Report Source specific fields are missing! Please check the following example:",
        example: exampleJDBCReportSourceJSON,
      });
    }

    // validate #/components/schemas/EmbeddedJDBCReportSource required values
    // declare an example in order to augment error messages when necessary
    let exampleEmbeddedJDBCReportSourceJSON = JSON.parse(
      `{
  "classname" : "gr.uoa.di.rdf.geordfbench.runtime.reportsource.impl.H2EmbeddedRepSrc",
  "driver" : "h2",
  "relativeBaseDir" : "../scripts/h2embeddedreportsource",
  "database" : "geordfbench",
  "user" : "sa",
  "password" : ""
}`
    );
    if (
      body.classname ===
        "gr.uoa.di.rdf.geordfbench.runtime.reportsource.impl.H2EmbeddedRepSrc" &&
      !body.hasOwnProperty("relativeBaseDir")
    ) {
      return res.status(403).json({
        error:
          "Embedded JDBC Report Source specific fields are missing! Please check the following example:",
        example: exampleEmbeddedJDBCReportSourceJSON,
      });
    }

    fs.writeFileSync(specFullPathName, JSON.stringify(body, null, 2));
    const result = JSON.parse(fs.readFileSync(specFullPathName));
    return res.status(201).json(result);
  });
} else {
  router.post("/:newspec", (req, res) => {
    return res
      .status(404)
      .json(
        `${specEntity} create functionality is disabled by the endpoint's configuration!`
      );
  });
}

if (canDelete) {
  // Delete an existing report source specification by name
  router.delete("/:existingspec", (req, res) => {
    const spec = req.params.existingspec;
    // check if spec is a filename with .json file type
    const specFullPathName =
      path.extname(spec) !== ".json"
        ? path.join(targetDir, spec + ".json")
        : path.join(targetDir, spec);
    console.log(
      `Will be deleting a report source specification file in the path ${specFullPathName}`
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
  });
} else {
  router.delete("/:existingspec", (req, res) => {
    return res
      .status(404)
      .json(
        `${specEntity} delete functionality is disabled by the endpoint's configuration!`
      );
  });
}

module.exports = router;
