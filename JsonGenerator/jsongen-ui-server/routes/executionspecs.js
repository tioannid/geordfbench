var express = require("express");
var router = express.Router();
const { HTTP } = require("http-call");
const pug = require("pug");
const fs = require("fs");
const path = require("path");

const specCategory = "executionspecs";
const specEntity = "Execution";

// get a list of report specifications for use with a
// PUG template
router.get("/", async (req, res) => {
  const Title = `List of ${specEntity} Specifications`;
  const endpointUrl = `${req.app.locals.endpointConfig.ENDPOINT_URL}/${specCategory}`;
  const { body: specs } = await HTTP.get(endpointUrl);
  const UIUrl = `${req.app.locals.endpointConfig.UI_URL}/${specCategory}`;
  return res.render(`${specCategory}/list`, {
    title: Title,
    postBaseUrl: `${endpointUrl}`,
    records: specs,
    UIUrl: `${UIUrl}`,
    specEntity: specEntity,
  });
});

// render a "New Execution Specification" form
router.get("/new", async (req, res, next) => {
  const Title = `Create New ${specEntity} Specification`;
  res.render(`${specCategory}/new`, {
    title: Title,
    postBaseUrl: `${req.app.locals.endpointConfig.ENDPOINT_URL}/${specCategory}`,
    endpointConfig: req.app.locals.endpointConfig,
  });
});

// render a "New Execution Specification" form based on an existing specification
router.get("/new/:existingspec", async (req, res, next) => {
  const Title = `Create New ${specEntity} Specification (from an existing one)`;
  const existingspecname = req.params.existingspec;
  const specfilenameonly = path.parse(existingspecname).name;
  // try to retrieve the existingspec from the JSON Library endpoint
  var url = `${req.app.locals.endpointConfig.ENDPOINT_URL}/${specCategory}/${existingspecname}`;
  const copyURL = `${req.app.locals.endpointConfig.UI_URL}/${specCategory}/new/${existingspecname}`;
  try {
    const { body: existingSpec } = await HTTP.get(url);
    // change the specification name
    existingSpec.name = specfilenameonly + " (copy)";
    res.render(`${specCategory}/new`, {
      title: Title,
      postBaseUrl: `${req.app.locals.endpointConfig.ENDPOINT_URL}/${specCategory}`,
      endpointConfig: req.app.locals.endpointConfig,
      existingSpec: existingSpec,
      copyURL: copyURL,
    });
  } catch (error) {
    console.error(error.message);
  }
});

module.exports = router;
