const express = require("express");
const fs = require("fs");
const path = require("path");

var router = express.Router();

const specCategory = "querysets";
const specEntity = "Queryset";
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

// get a list of queryset specifications
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

// Retrieve a queryset specification
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

// Send a queryset specification to a JSON file
router.post("/:newspec", (req, res) => {
  if (res.app.locals.canCreate) {
    const spec = req.params.newspec;
    // check if spec is a filename with .json file type
    const specFullPathName =
      path.extname(spec) !== ".json"
        ? path.join(targetDir, spec + ".json")
        : path.join(targetDir, spec);
    console.log(
      `Will be creating a queryset specification file in the path ${specFullPathName}`
    );
    if (fs.existsSync(specFullPathName)) {
      return res
        .status(404)
        .json({ error: `${specFullPathName} already exists!` });
    }

    // grap the POST body
    const body = req.body;
    // validate #/components/schemas/SimpleQueryset required values
    // declare an example in order to augment error messages when necessary
    let exampleSimpleQuerysetJSON = JSON.parse(
      JSON.stringify(
        `{
    "classname": "gr.uoa.di.rdf.geordfbench.runtime.querysets.complex.impl.SimpleQS",
    "name": "scalabilityFunc",
    "relativeBaseDir": "",
    "hasPredicateQueriesAlso": false,
    "mapQueries": {
        "0": {
            "label": "SC1_Geometries_Intersects_GivenPolygon",
            "text": "SELECT ?s1 ?o1 WHERE { \n ?s1 geo:asWKT ?o1 . \n  FILTER(geof:sfIntersects(?o1, \"POLYGON((23.708496093749996 37.95719224376526,22.906494140625 40.659805938378526, 23.708496093749996 37.95719224376526))\"^^<http://www.opengis.net/ont/geosparql#wktLiteral>)). \n} \n",
            "usePredicate": false,
            "expectedResults": -1
        }
    },
    "mapUsefulNamespacePrefixes": {}
}`
      )
    );
    if (
      !(
        body.hasOwnProperty("classname") &&
        body.hasOwnProperty("name") &&
        body.hasOwnProperty("relativeBaseDir") &&
        body.hasOwnProperty("hasPredicateQueriesAlso") &&
        body.hasOwnProperty("mapQueries") &&
        body.hasOwnProperty("mapUsefulNamespacePrefixes")
      )
    ) {
      return res.status(400).json({
        error:
          "SimpleQueryset fields are missing! Please check the following example:",
        example: exampleSimpleQuerysetJSON,
      });
    }

    // validate #/components/schemas/StaticTempParamQueryset required values
    // declare an example in order to augment error messages when necessary
    let exampleStaticTempParamQuerysetJSON = JSON.parse(
      JSON.stringify(
        `{
  "classname" : "gr.uoa.di.rdf.geordfbench.runtime.querysets.complex.impl.StaticTempParamQS",
  "name" : "scalabilityFunc",
  "relativeBaseDir" : "",
  "hasPredicateQueriesAlso" : false,
  "mapQueries" : {
    "0" : {
      "label" : "SC1_Geometries_Intersects_GivenPolygon",
      "text" : "SELECT ?s1 ?o1 WHERE { \n ?s1 geo:asWKT ?o1 . \n  FILTER(geof:FUNCTION(?o1, GIVEN_SPATIAL_LITERAL)). \n} \n",
      "usePredicate" : false,
      "expectedResults" : -1
    },
    "1" : {
      "label" : "SC2_Intensive_Geometries_Intersect_Geometries",
      "text" : "SELECT ?s1 ?s2 \nWHERE { \n ?s1 geo:hasGeometry [ geo:asWKT ?o1 ] ;\n    lgo:has_code \"1001\"^^xsd:integer . \n ?s2 geo:hasGeometry [ geo:asWKT ?o2 ] ;\n    lgo:has_code ?code2 .  \n FILTER(?code2>5000 && ?code2<6000 && ?code2 != 5260) .\n FILTER(geof:FUNCTION(?o1, ?o2)). \n} \n",
      "usePredicate" : false,
      "expectedResults" : -1
    },
    "2" : {
      "label" : "SC3_Relaxed_Geometries_Intersect_Geometries",
      "text" : "SELECT ?s1 ?s2\nWHERE {\n ?s1 geo:hasGeometry [ geo:asWKT ?o1 ] ;\n    lgo:has_code \"1001\"^^xsd:integer .\n ?s2 geo:hasGeometry [ geo:asWKT ?o2 ] ;\n    lgo:has_code ?code2 .\n FILTER(?code2 IN (5622, 5601, 5641, 5621, 5661)) .\n FILTER(geof:FUNCTION(?o1, ?o2)).\n} \n",
      "usePredicate" : false,
      "expectedResults" : -1
    }
  },
  "mapUsefulNamespacePrefixes" : { },
  "mapTemplateParams" : {
    "GIVEN_SPATIAL_LITERAL" : "\"POLYGON((23.708496093749996 37.95719224376526,22.906494140625 40.659805938378526,11.524658203125002 48.16425348854739,-0.1181030273437499 51.49506473014367,-3.2189941406250004 55.92766341247031,-5.940856933593749 54.59116279530599,-3.1668090820312504 51.47967237816337,23.708496093749996 37.95719224376526))\"^^<http://www.opengis.net/ont/geosparql#wktLiteral>",
    "FUNCTION" : "sfIntersects"
  },
  "mapGraphPrefixes" : { }
}`
      )
    );
    if (
      body.classname ===
        "gr.uoa.di.rdf.geordfbench.runtime.querysets.complex.impl.StaticTempParamQS" &&
      !(
        body.hasOwnProperty("mapTemplateParams") &&
        body.hasOwnProperty("mapGraphPrefixes")
      )
    ) {
      return res.status(403).json({
        error:
          "StaticTempParamQS specific fields are missing! Please check the following example:",
        example: exampleStaticTempParamQuerysetJSON,
      });
    }

    // validate #/components/schemas/DynamicTempParamQueryset required values
    // declare an example in order to augment error messages when necessary
    let exampleDynamicTempParamQuerysetJSON = JSON.parse(
      JSON.stringify(
        `{
  "classname" : "gr.uoa.di.rdf.geordfbench.runtime.querysets.complex.impl.DynamicTempParamQS",
  "name" : "rwmicro",
  "relativeBaseDir" : "",
  "hasPredicateQueriesAlso" : false,
  "mapQueries" : {
    "0" : {
      "label" : "Buffer_GeoNames",
      "text" : "NONTOP_BUFFER",
      "usePredicate" : false,
      "expectedResults" : 21990
    },
    "1" : {
      "label" : "Buffer_LGD",
      "text" : "NONTOP_BUFFER",
      "usePredicate" : false,
      "expectedResults" : 12097
    },
    "2" : {
      "label" : "Within_GeoNames_Point_Buffer",
      "text" : "SELECT_BUFFER",
      "usePredicate" : false,
      "expectedResults" : 152
    },
    "3" : {
      "label" : "Touches_GAG_GAG",
      "text" : "JOIN_2",
      "usePredicate" : false,
      "expectedResults" : 1022
    }
  },
  "mapUsefulNamespacePrefixes" : {
    "uom" : "<http://www.opengis.net/def/uom/OGC/1.0/>",
    "strdf" : "<http://strdf.di.uoa.gr/ontology#>"
  },
  "mapQueryTemplates" : {
    "SELECT_BUFFER" : "SELECT ?s1 \nWHERE {\n\tGRAPH <GRAPH1> {\n\t\t?s1 <ASWKT1> ?o1\n\t}\nFILTER(geof:<FUNCTION1>(?o1, geof:<FUNCTION2>(\"POINT(23.71622 37.97945)\"^^<http://www.opengis.net/ont/geosparql#wktLiteral>, 3000, uom:metre))).\n}",
    "NONTOP_BUFFER" : "SELECT (geof:<FUNCTION1>(?o1, 4, uom:metre) AS ?ret) \nWHERE {\n\tGRAPH <GRAPH1> {\n\t\t?s1 <ASWKT1> ?o1\n\t}\n}",
    "JOIN_2" : "SELECT ?s1 ?s2 \nWHERE { \n\tGRAPH <GRAPH1> {\n\t\t?s1 <ASWKT1> ?o1\n\t}\n\tGRAPH <GRAPH2> {\n\t\t?s2 <ASWKT2> ?o2\n\t}\nFILTER(?s1 != ?s2).\nFILTER(geof:<FUNCTION1>(?o1, ?o2)).\n}"
  },
  "mapLiteralValues" : {
    "LL_clc" : "<http://geographica.di.uoa.gr/dataset/clc>",
    "LL_clc_asWKT" : "<http://geo.linkedopendata.gr/corine/ontology#asWKT>",
    "LL_gadm_asWKT" : "<http://geo.linkedopendata.gr/gag/ontology/asWKT>",
    "LL_geonames_asWKT" : "<http://www.geonames.org/ontology#asWKT>",
    "LL_fn_area" : "area",
    "LL_sfWithin" : "sfWithin",
    "LL_lgd" : "<http://geographica.di.uoa.gr/dataset/lgd>",
    "LL_gadm" : "<http://geographica.di.uoa.gr/dataset/gag>",
    "LL_sfTouches" : "sfTouches",
    "LL_geonames" : "<http://geographica.di.uoa.gr/dataset/geonames>",
    "LL_lgd_asWKT" : "<http://linkedgeodata.org/ontology/asWKT>",
    "LL_fn_buffer" : "buffer"
  },
  "templateDynamicParamNameList" : [ "<FUNCTION1>", "<FUNCTION2>", "<ASWKT1>", "<ASWKT2>", "<GRAPH1>", "<GRAPH2>" ],
  "templateParamValueMatrix" : {
    "mapTemplateParamValues" : {
      "0##<GRAPH1>" : "LL_geonames",
      "1##<GRAPH1>" : "LL_lgd",
      "2##<FUNCTION2>" : "LL_fn_buffer",
      "2##<FUNCTION1>" : "LL_sfWithin",
      "1##<ASWKT1>" : "LL_lgd_asWKT",
      "3##<FUNCTION1>" : "LL_sfTouches",
      "2##<GRAPH1>" : "LL_geonames",
      "2##<ASWKT1>" : "LL_geonames_asWKT",
      "3##<GRAPH2>" : "LL_gadm",
      "3##<GRAPH1>" : "LL_gadm",
      "3##<ASWKT2>" : "LL_gadm_asWKT",
      "3##<ASWKT1>" : "LL_gadm_asWKT",
      "1##<FUNCTION1>" : "LL_fn_buffer",
      "0##<ASWKT1>" : "LL_geonames_asWKT",
      "0##<FUNCTION1>" : "LL_fn_buffer"
    }
  },
  "isTranslated" : false
}`
      )
    );
    if (
      body.classname ===
        "gr.uoa.di.rdf.geordfbench.runtime.querysets.complex.impl.DynamicTempParamQS" &&
      !(
        body.hasOwnProperty("mapQueryTemplates") &&
        body.hasOwnProperty("mapLiteralValues") &&
        body.hasOwnProperty("templateDynamicParamNameList") &&
        body.hasOwnProperty("templateParamValueMatrix") &&
        body.hasOwnProperty("isTranslated")
      )
    ) {
      return res.status(403).json({
        error:
          "DynamicTempParamQueryset specific fields are missing! Please check the following example:",
        example: exampleDynamicTempParamQuerysetJSON,
      });
    }

    // TODO: We need to repeat the previous pattern for all SimpleQueryset subclasses such as:
    //       MacroGeocodingQueryset, MacroMapSearchQueryset, etc.

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

// Delete an existing queryset specification by name
router.delete("/:existingspec", (req, res) => {
  if (res.app.locals.canDelete) {
    const spec = req.params.existingspec;
    // check if spec is a filename with .json file type
    const specFullPathName =
      path.extname(spec) !== ".json"
        ? path.join(targetDir, spec + ".json")
        : path.join(targetDir, spec);
    console.log(
      `Will be deleting a queryset specification file in the path ${specFullPathName}`
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
