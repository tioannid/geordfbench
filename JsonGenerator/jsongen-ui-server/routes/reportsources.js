var express = require("express");
var router = express.Router();
const { HTTP } = require("http-call");
const pug = require("pug");
const fs = require("fs");
const path = require("path");

const specCategory = "reportsources";
const specEntity = "Report Source";

// A JSON database which will hold the 'existingSpec'
// pushed by a router.post("/clone") and
// popped by a the router.get("/cloned").
// The key for pushing and popping will be the 'specfilenameonly'
var db = new Map();

// available types of Report Sources and their dependent fields default values
const validtypes = ["postgresql", "h2embedded"];
const types = [
  {
    name: validtypes[0], // "postgresql"
    items: {
      menuname: "PostgreSQL",
      specDefaultValues: {
        classname:
          "gr.uoa.di.rdf.geordfbench.runtime.reportsource.impl.PostgreSQLRepSrc",
        name: "Report Source name",
        driver: "postgresql",
        hostname: "192.168.1.1",
        althostname: "localhost",
        port: 5432,
        database: "geordfbench",
        user: "geordfbench",
        password: "geordfbench",
      },
    },
  },
  {
    name: validtypes[1], // "h2embedded"
    items: {
      menuname: "H2 Embedded",
      specDefaultValues: {
        classname:
          "gr.uoa.di.rdf.geordfbench.runtime.reportsource.impl.H2EmbeddedRepSrc",
        name: "Report Source name",
        driver: "h2",
        relativeBaseDir: "../scripts/h2embeddedreportsource",
        database: "geordfbench",
        user: "sa",
        password: "",
      },
    },
  },
];

// get a list of host specifications for use with a
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
    }
    // (err, html) => {
    //   res.send(html);
    // }
  );
});

// render a "New Report Source Specification" form
router.get("/new/type/:typename", async (req, res, next) => {
  const typename = req.params.typename;
  // check if it is a valid type
  if (!validtypes.includes(typename)) {
    return; // TODO: send descriptive HTML error listing the allowed values
  }
  const Title = `Create New ${specEntity} Specification of type '${typename}'`;
  // return the specDefaultValues based on the type name
  const specDefaultValues = types.filter((type) => type.name == typename)[0]
    .items.specDefaultValues;
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
        copyURL: copyURL,
        pageMode: "new",
        cloneURL: cloneURL,
        types: types,
        specEntity: specEntity,
      },
      (err, html) => {
        // if (err) {
        //   console.error(err.message);
        //   return;
        // }
        // const output_html = path.join(
        //   __dirname,
        //   "../html/pug_reportsources_new.html"
        // );
        // fs.writeFileSync(output_html, html);
        res.send(html);
      }
    );
  } catch (error) {
    console.error(error.message);
  }
});

// render a "New Report Source Specification" form based either from an existing specification
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

  // return the specDefaultValues based on the type classname
  const specDefaultValues = types.filter(
    (type) => type.items.specDefaultValues.classname == existingSpec.classname
  )[0].items.specDefaultValues;

  // try to retrieve the existingspec from the JSON Library endpoint
  const copyURL = `${req.app.locals.endpointConfig.ACCESS_UI_URL}/${specCategory}/new/${existingspecname}`;
  const cloneURL = `${req.app.locals.endpointConfig.ACCESS_UI_URL}/${specCategory}/clone`;
  try {
    res.render(`${specCategory}/new`, {
      title: Title,
      postBaseUrl: `${req.app.locals.endpointConfig.ACCESS_ENDPOINT_URL}/${specCategory}`,
      endpointConfig: req.app.locals.endpointConfig,
      existingSpec: existingSpec,
      specDefaultValues: specDefaultValues,
      copyURL: copyURL,
      pageMode: pageMode,
      cloneURL: cloneURL,
      types: types,
      specEntity: specEntity,
    });
  } catch (error) {
    console.error(error.message);
  }
});

// Store a cloned "Report Source Specification" to the 'db' database.
// Used from a rendered (new or copy spec) page when either the number of detailed (child) records are being
// modified or when the main fields differ (concrete subclasses of the main abstract class exist)
// This router.post("/clone") is invoked by javascript code in the initial rendered HTML page on the client
// and it should always be followed by a router.get("/cloned") which will render the updated HTML page on the client.
router.post("/clone", async (req, res, next) => {
  // grap the POST body
  const existingSpec = req.body;
  const existingspecname = existingSpec.name;
  db.set(existingspecname, existingSpec);
  return res.status(200).json({
    msg: `Cloned ${specEntity.toLowerCase()} specification ${existingspecname} has been added to UI endpoints database`,
    dbitems: db,
  });
});

module.exports = router;
