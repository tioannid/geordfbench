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

// get the ACCESS_TOKEN_SECRET
global.ACCESS_TOKEN_SECRET = process.env.ACCESS_TOKEN_SECRET;

// get the REFRESH_TOKEN_SECRET
global.REFRESH_TOKEN_SECRET = process.env.REFRESH_TOKEN_SECRET;

const app = express();

// get the initial values for the Endpoint Configuration
app.locals.endpointConfig = {
  ENDPOINT_HOST: process.env.ENDPOINT_HOST,
  ENDPOINT_PORT: process.env.ENDPOINT_PORT,
  ENDPOINT_URL: `http://${process.env.ENDPOINT_HOST}:${process.env.ENDPOINT_PORT}`,
  PORT: process.env.PORT,
};

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
  // { root: "/admin", router: require("./routes/admin") },
];

for (rootPair of mapRouters) {
  app.use(rootPair.root, rootPair.router);
}

// Error handling middleware
app.use((err, req, res, next) => {
  console.error('Error:', err);
  res.status(err.status || 500).render('error', {
    title: 'Error',
    message: err.message || 'Internal Server Error',
    error: process.env.NODE_ENV === 'development' ? err : {}
  });
});

// 404 handler
app.use((req, res) => {
  res.status(404).render('error', {
    title: '404 - Page Not Found',
    message: `The page ${req.originalUrl} was not found`,
    error: {}
  });
});

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

const port = process.env.PORT || 5001;
app.listen(port, () => {
  console.log(
    `${packageJson.description} v${packageJson.version} is up and running at port ${port}!`
  );
});
