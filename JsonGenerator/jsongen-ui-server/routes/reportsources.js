var express = require("express");
var router = express.Router();
const { HTTP } = require("http-call");
const pug = require("pug");
const fs = require("fs");
const path = require("path");

const specCategory = "reportsources";
const specEntity = "Report Source";

// Define default values for Report Sources through
// a report source specification object initializer
// for one of thw PostgreSQL report source type
const specDefaultValues = {
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
};

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
router.get("/new", async (req, res, next) => {
  const Title = `Create New ${specEntity} Specification`;
  const url = `${req.app.locals.endpointConfig.ACCESS_ENDPOINT_URL}/oses`;
  const { body: oses } = await HTTP.get(url);
  res.render(`${specCategory}/new`, {
    title: Title,
    postBaseUrl: `${req.app.locals.endpointConfig.ACCESS_ENDPOINT_URL}/${specCategory}`,
    endpointConfig: req.app.locals.endpointConfig,
    existingSpec: specDefaultValues,
    specDefaultValues: specDefaultValues,
    copyURL: copyURL,
    pageMode: "new",
    cloneURL: cloneURL,
    types: ["PostgreSQL", "H2 Embedded"],
  });
});

// render a "New Report Source Specification" form based on an existing specification
router.get("/new/:existingspec", async (req, res, next) => {
  const Title = `Create New ${specEntity} Specification (from an existing one)`;
  const existingspecname = req.params.existingspec;
  // try to retrieve the existingspec from the JSON Library endpoint
  var url = `${req.app.locals.endpointConfig.ACCESS_ENDPOINT_URL}/${specCategory}/${existingspecname}`;
  const copyURL = `${req.app.locals.endpointConfig.ACCESS_UI_URL}/${specCategory}/new/${existingspecname}`;
  const { body: existingSpec } = await HTTP.get(url);
  // change the specification name
  existingSpec.name = existingSpec.name + " (copy)";
  url = `${req.app.locals.endpointConfig.ACCESS_ENDPOINT_URL}/oses`;
  const { body: oses } = await HTTP.get(url);
  res.render(`${specCategory}/new`, {
    title: Title,
    postBaseUrl: `${req.app.locals.endpointConfig.ACCESS_ENDPOINT_URL}/${specCategory}`,
    endpointConfig: req.app.locals.endpointConfig,
    oses: oses,
    getOsesUrl: `${url}`,
    existingSpec: existingSpec,
    copyURL: copyURL,
  });
});

module.exports = router;
