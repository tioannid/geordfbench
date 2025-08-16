const express = require("express");
const cors = require("cors");
const path = require("path");
const packageJson = require("./package.json");
const logger = require("morgan");
const swaggerUi = require("swagger-ui-express");
const fs = require("fs");
const YAML = require("yaml");
const os = require("os");

// run dotenv
require("dotenv").config();

// get the JSON Library path
global.json_lib_path = process.env.JSON_LIB_PATH;

// get the Allowed Operations Per Specification Class
global.crud_map = JSON.parse(process.env.CRUD_MAP);

// get the ACCESS_TOKEN_SECRET
global.ACCESS_TOKEN_SECRET = process.env.ACCESS_TOKEN_SECRET;

// get the REFRESH_TOKEN_SECRET
global.REFRESH_TOKEN_SECRET = process.env.REFRESH_TOKEN_SECRET;

const app = express();
// configure app
app.use(express.json());
app.use(express.urlencoded({ extended: true }));
const corsOptions = {
  origin: [`http://localhost/`],
};
app.use(cors(corsOptions));
// app.use(cors({ origin: /http:\/\/localhost/ }));
// app.options("*", cors());
app.use("/static", express.static("public"));

// view engine setup
app.set("views", path.join(__dirname, "views"));
app.set("view engine", "pug");

/**
 * Configure 'morgan' HTTP request logger middleware.
 * 'dev': Concise output colored by response status for development use.
 */
app.use(logger("dev"));

// get the initial values for the Endpoint Configuration
app.locals.endpointConfig = {
  PORT: process.env.PORT,
  JSON_LIB_PATH: process.env.JSON_LIB_PATH,
  HOSTNAME: os.hostname(),
  PLATFORM: os.version().concat(" v", os.release(), ", ", os.machine()),
  CRUD_MAP: JSON.parse(process.env.CRUD_MAP),
};

/**
 * Assign all roots and router pairs with a
 * loop over an array of objects.
/** @type {*} */
const mapRouters = [
  { root: "/", router: require("./routes/home") },
  { root: "/categories", router: require("./routes/categories") }, // JSON Library Specification Categories
  { root: "/executionspecs", router: require("./routes/executionspecs") },
  { root: "/hosts", router: require("./routes/hosts") },
  { root: "/datasets", router: require("./routes/datasets") },
  { root: "/querysets", router: require("./routes/querysets") },
  { root: "/reportspecs", router: require("./routes/reportspecs") },
  { root: "/reportsources", router: require("./routes/reportsources") },
  { root: "/workloads", router: require("./routes/workloads") },
  { root: "/admin", router: require("./routes/admin") },
];

for (rootPair of mapRouters) {
  app.use(rootPair.root, rootPair.router);
}

// Initialize swagger-jsdoc -> returns validated swagger spec in json format
const swaggerInitSpecification = fs.readFileSync(
  "./openapi/swaggerInit.yaml",
  "utf8"
);
const swaggerDocument = YAML.parse(swaggerInitSpecification);
app.use(
  "/api-docs",
  swaggerUi.serve,
  swaggerUi.setup(swaggerDocument, { explorer: true })
);

const port = process.env.PORT || 5000;
app.listen(port, () => {
  console.log(
    `${packageJson.description} v${packageJson.version} is up and running at port ${port}!`
  );
});
