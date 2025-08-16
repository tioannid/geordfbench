# GeoRDFBench Framework - JSON Specification UI Server

A comprehensive web-based user interface for managing GeoRDFBench Framework JSON specifications.

## Overview

This UI server provides a complete CRUD (Create, Read, Update, Delete) interface for all GeoRDFBench specification categories:

- **Datasets** - Define RDF data sources and their characteristics
- **Execution Specifications** - Configure experiment execution parameters  
- **Hosts** - Specify target execution environments
- **Query Sets** - Define collections of SPARQL/GeoSPARQL queries
- **Report Sources** - Configure result reporting mechanisms
- **Report Specifications** - Define logging and output parameters
- **Workloads** - Combine datasets, query sets, and execution specifications

## Features

### ✅ Complete CRUD Operations
- **Create** - User-friendly forms for creating new specifications
- **Read** - List views with search and filtering capabilities
- **Update** - Edit forms with pre-populated data and validation
- **Delete** - Confirmation dialogs with detailed information

### ✅ Modern Web Interface
- **Bootstrap 5** - Responsive design for all screen sizes
- **Pug Templates** - Clean, maintainable HTML generation
- **Client-side Validation** - Real-time form validation and feedback
- **Error Handling** - Comprehensive error messages and recovery

### ✅ Advanced Features
- **Dashboard** - Overview of all specification categories with counts
- **Navigation Menu** - Easy access to all specification types
- **JSON Validation** - Schema validation for complex JSON structures
- **API Integration** - Seamless communication with endpoint server

## Architecture

The UI server follows a clean MVC architecture:

```
jsongen-ui-server/
├── jsongen-ui-server.js     # Main application server
├── routes/                  # Express.js route handlers
│   ├── home.js             # Dashboard and home page
│   ├── datasets.js         # Dataset CRUD operations
│   ├── executionspecs.js   # Execution spec CRUD operations
│   ├── hosts.js            # Host CRUD operations
│   ├── querysets.js        # Query set CRUD operations
│   ├── reportsources.js    # Report source CRUD operations
│   ├── reportspecs.js      # Report spec CRUD operations
│   └── workloads.js        # Workload CRUD operations
├── views/                  # Pug templates
│   ├── layout.pug          # Base layout with navigation
│   ├── home.pug            # Dashboard page
│   ├── error.pug           # Error page template
│   └── [category]/         # Category-specific templates
│       ├── list.pug        # List view
│       ├── new.pug         # Create form
│       ├── edit.pug        # Edit form
│       └── delete.pug      # Delete confirmation
└── public/                 # Static assets
```

## Installation & Setup

### Prerequisites
- Node.js (v14 or higher)
- GeoRDFBench Endpoint Server running

### Installation
```bash
cd JsonGenerator/jsongen-ui-server
npm install
```

### Configuration
Create a `.env` file with the following variables:
```env
PORT=5001
ENDPOINT_HOST=localhost
ENDPOINT_PORT=5000
NODE_ENV=development
```

### Running the Server
```bash
# Development mode with auto-reload
npm run dev

# Production mode
npm start
```

The server will be available at `http://localhost:5001`

## Usage

### Dashboard
The home page provides an overview of all specification categories with:
- Count of specifications in each category
- Quick access links to manage each category
- System information and status

### Managing Specifications

#### Creating New Specifications
1. Navigate to the desired category (e.g., Datasets)
2. Click "New [Category] Specification"
3. Fill out the form with required information
4. Submit to create the specification

#### Editing Existing Specifications
1. Go to the category list view
2. Click "Edit" next to the specification you want to modify
3. Update the form fields as needed
4. Submit to save changes

#### Deleting Specifications
1. Go to the category list view
2. Click "Delete" next to the specification
3. Review the confirmation dialog
4. Confirm deletion if you're sure

### Form Validation
All forms include comprehensive validation:
- **Required fields** - Highlighted with error messages
- **Data types** - Numeric fields validated for proper format
- **JSON syntax** - Complex JSON fields validated for syntax
- **Business rules** - Category-specific validation rules

## API Integration

The UI server communicates with the GeoRDFBench Endpoint Server via REST API:

- **GET** `/[category]` - List all specifications
- **GET** `/[category]/[spec]` - Get specific specification
- **POST** `/[category]/[spec]` - Create new specification
- **PUT** `/[category]/[spec]` - Update existing specification
- **DELETE** `/[category]/[spec]` - Delete specification

## Error Handling

Comprehensive error handling at multiple levels:

### Client-Side
- Form validation with real-time feedback
- Network error handling with user-friendly messages
- Loading states and progress indicators

### Server-Side
- Express.js error middleware
- 404 handling for missing pages
- API communication error handling
- Development vs production error display

## Development

### Adding New Specification Categories

1. **Create Router** - Add new router in `routes/[category].js`
2. **Create Views** - Add templates in `views/[category]/`
3. **Update Navigation** - Add links in `layout.pug`
4. **Register Route** - Add to `mapRouters` in main server file

### Customizing Forms

Forms can be customized by modifying the Pug templates:
- **Field Types** - Input, select, textarea, checkbox
- **Validation** - HTML5 validation attributes
- **Styling** - Bootstrap classes and custom CSS
- **JavaScript** - Client-side form handling

### Testing

```bash
# Run development server
npm run dev

# Test all CRUD operations for each category
# Verify form validation and error handling
# Check responsive design on different screen sizes
```

## Troubleshooting

### Common Issues

**Server won't start**
- Check if port 5001 is available
- Verify Node.js version (v14+)
- Ensure all dependencies are installed

**Can't connect to endpoint server**
- Verify endpoint server is running on configured port
- Check ENDPOINT_HOST and ENDPOINT_PORT in .env
- Ensure network connectivity between servers

**Forms not submitting**
- Check browser console for JavaScript errors
- Verify form validation is passing
- Check network tab for API request failures

**Styling issues**
- Ensure Bootstrap CSS is loading correctly
- Check for conflicting CSS rules
- Verify responsive breakpoints

### Logs and Debugging

Enable detailed logging by setting:
```env
NODE_ENV=development
```

This will show:
- Detailed error stack traces
- API request/response logging
- Form submission debugging

## Contributing

When contributing to the UI server:

1. Follow the existing code structure and patterns
2. Add comprehensive error handling
3. Include form validation for new fields
4. Test all CRUD operations thoroughly
5. Update documentation for new features

## License

This project is part of the GeoRDFBench Framework and follows the same Apache 2.0 license.

## Support

For issues and questions:
- Check the troubleshooting section above
- Review the GeoRDFBench Framework documentation
- Contact the development team

---

**GeoRDFBench Framework** - Making geospatial semantic benchmarking accessible to everyone.
