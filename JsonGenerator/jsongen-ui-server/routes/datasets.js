var express = require("express");
var router = express.Router();
const { HTTP } = require("http-call");
const pug = require("pug");
const fs = require("fs");
const path = require("path");

const specCategory = "datasets";
const specEntity = "Dataset";

// A JSON database which will hold the 'existingSpec'
// pushed by a router.post("/clone") and
// popped by a the router.get("/cloned").
// The key for pushing and popping will be the 'specfilenameonly'
var db = new Map();

// Compile the PUG source files
const viewsbasedir = path.join(__dirname, "../views");
const listCompiledFunction = pug.compileFile(
  `${viewsbasedir}/${specCategory}/list.pug`
);
const newCompiledFunction = pug.compileFile(
  `${viewsbasedir}/${specCategory}/new.pug`
);

// Define default values for Simple Datasets
const childSpecDefaultValues = {
  name: "Simple Dataset Name", // it has spaces and this causes extra problem in DOM manipulation
  relativeBaseDir: "",
  dataFile: "datafile.nt",
  rdfFormat: "N-TRIPLES",
  mapUsefulNamespacePrefixes: {},
  mapAsWKT: {},
  mapHasGeometry: {},
};

// Define default values for Complex Datasets through
// a dataset specification object initializer
const specDefaultValues = {
  classname:
    "gr.uoa.di.rdf.geordfbench.runtime.datasets.complex.impl.GeographicaDS",
  name: "Dataset name",
  n: 0,
  relativeBaseDir: "",
  simpleGeospatialDataSetList: [childSpecDefaultValues],
  mapDataSetContexts: {},
};

// get a list of dataset specifications for use with a
// PUG template
router.get("/", async (req, res) => {
  const Title = `List of ${specEntity} Specifications`;
  const endpointUrl = `${req.app.locals.endpointConfig.ACCESS_ENDPOINT_URL}/${specCategory}`;
  const { body: specs } = await HTTP.get(endpointUrl);
  const UIUrl = `${req.app.locals.endpointConfig.ACCESS_UI_URL}/${specCategory}`;
  return res.render(
    `${specCategory}/list`,
    {
      title: Title,
      postBaseUrl: `${endpointUrl}`,
      records: specs,
      UIUrl: `${UIUrl}`,
      specEntity: specEntity,
    },
    (err, html) => {
      res.send(html);
    }
  );
});

// render a "New Dataset Specification" form
router.get("/new", async (req, res, next) => {
  const Title = `Create New ${specEntity} Specification`;
  var existingSpec = {};
  const existingspecname = specDefaultValues.name;
  // try to retrieve the existingspec from the JSON Library endpoint
  const copyURL = `${req.app.locals.endpointConfig.ACCESS_UI_URL}/${specCategory}/new/${existingspecname}`;
  const cloneURL = `${req.app.locals.endpointConfig.ACCESS_UI_URL}/${specCategory}/clone`;
  try {
    res.render(
      `${specCategory}/new`,
      {
        title: Title,
        postBaseUrl: `${req.app.locals.endpointConfig.ACCESS_ENDPOINT_URL}/${specCategory}`,
        endpointConfig: req.app.locals.endpointConfig,
        existingSpec: specDefaultValues,
        specDefaultValues: specDefaultValues,
        childSpecDefaultValues: childSpecDefaultValues,
        copyURL: copyURL,
        pageMode: "new",
        cloneURL: cloneURL,
      },
      (err, html) => {
        if (err) {
          console.error(err.message);
        }
        // const output_html = path.join(
        //   __dirname,
        //   "../html/pug_datasets_new.html"
        // );
        // fs.writeFileSync(output_html, html);
        res.send(html);
      }
    );
  } catch (error) {
    console.error(error.message);
  }
});

// render a "New Dataset Specification" form based either from an existing specification
// or from a transient specification stored in this module's 'db' map database
router.get("/new/:existingspec", async (req, res, next) => {
  const Title = `Create New ${specEntity} Specification (from an existing one)`;
  const existingspecname = req.params.existingspec;
  const specfilenameonly = path.parse(existingspecname).name;

  // Fill the existingSpec variable:
  // - check if there is an entry in the 'db' database (map) with 'specfilenameonly' as key
  //   and then return the value and remove it from the 'db' database
  // - otherwise retrieve it from the JSON Library endpoint with a call
  var existingSpec = {};
  var pageMode = "";
  if (db.has(specfilenameonly)) {
    existingSpec = db.get(specfilenameonly);
    if (!db.delete(specfilenameonly)) {
      console.error(
        `Transient specification ${specfilenameonly} found in local database, but could not remove it!`
      );
      return;
    }
    pageMode = "clone";
  } else {
    try {
      const response = await HTTP.get(
        `${req.app.locals.endpointConfig.ACCESS_ENDPOINT_URL}/${specCategory}/${existingspecname}`
      );
      existingSpec = response.body;
      // change the specification name
      existingSpec.name = specfilenameonly + "_copy";
      pageMode = "copy";
    } catch (error) {
      console.error(error.message);
    }
  }

  // try to retrieve the existingspec from the JSON Library endpoint
  const copyURL = `${req.app.locals.endpointConfig.ACCESS_UI_URL}/${specCategory}/new/${existingspecname}`;
  const cloneURL = `${req.app.locals.endpointConfig.ACCESS_UI_URL}/${specCategory}/clone`;
  try {
    res.render(
      `${specCategory}/new`,
      {
        title: Title,
        postBaseUrl: `${req.app.locals.endpointConfig.ACCESS_ENDPOINT_URL}/${specCategory}`,
        endpointConfig: req.app.locals.endpointConfig,
        existingSpec: existingSpec,
        childSpecDefaultValues: childSpecDefaultValues,
        copyURL: copyURL,
        pageMode: pageMode,
        cloneURL: cloneURL,
      },
      (err, html) => {
        // const output_html = path.join(
        //   __dirname,
        //   "../html/pug_datasets_new.html"
        // );
        // fs.writeFileSync(output_html, html);
        res.send(html);
      }
    );
  } catch (error) {
    console.error(error.message);
  }
});

// Store a cloned "Dataset Specification" to the 'db' database.
// Used from a rendered (new or copy spec) page when the number of detailed (child) records are being
// modified.
// This router.post("/clone") is invoked by javascript code in the initial rendered HTML page on the client
// and it should always be followed by a router.get("/cloned") which will render the updated HTML page on the client.
router.post("/clone", async (req, res, next) => {
  // grap the POST body
  const existingSpec = req.body;
  const existingspecname = existingSpec.name;
  db.set(existingspecname, existingSpec);
  return res.status(200).json({
    msg: `Cloned dataset specification ${existingspecname} has been added to UI endpoints database`,
    dbitems: db,
  });
});

module.exports = router;
