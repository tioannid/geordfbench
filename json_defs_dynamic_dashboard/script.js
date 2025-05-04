const ErrorBoundary = function(props) {
  const [error, setError] = React.useState(null);
  React.useEffect(function() {
    window.onerror = function(message) {
      setError(message);
      return true;
    };
    return function() {
      window.onerror = null;
    };
  }, []);
  if (error) {
    return React.createElement(
      "div",
      { className: "max-w-7xl mx-auto p-6" },
      React.createElement(
        "div",
        { className: "glassmorphism p-8" },
        React.createElement("h1", { className: "text-2xl font-bold text-red-600" }, "Error"),
        React.createElement("p", { className: "mt-2 text-white" }, "An error occurred: ", error),
        React.createElement("p", { className: "mt-2 text-white" }, "Please refresh the page or check the console for details.")
      )
    );
  }
  return props.children;
};

const Web3Animation = function() {
  const canvasRef = React.useRef(null);

  React.useEffect(function() {
    const canvas = canvasRef.current;
    const ctx = canvas.getContext('2d');
    canvas.width = 500;
    canvas.height = 400;

    const nodes = [
      { id: 'Ontology', x: 150, y: 100, label: 'Ontology', metrics: 'Schema Definition', glow: 0 },
      { id: 'RDF Store', x: 300, y: 120, label: 'RDF Store', metrics: 'Geographica2: Query Perf.', glow: 0 },
      { id: 'Spatial Fn', x: 350, y: 250, label: 'Spatial Fn', metrics: 'GeoSPARQL Support', glow: 0 },
      { id: 'Geobenchmark', x: 200, y: 300, label: 'Geobenchmark', metrics: 'Scalability Metrics', glow: 0 },
    ];
    const edges = [
      { from: 'Ontology', to: 'RDF Store' },
      { from: 'RDF Store', to: 'Spatial Fn' },
      { from: 'Spatial Fn', to: 'Geobenchmark' },
      { from: 'Geobenchmark', to: 'Ontology' },
    ];

    let time = 0;
    let hoveredNode = null;

    function drawGlow(x, y, radius, intensity) {
      ctx.beginPath();
      const gradient = ctx.createRadialGradient(x, y, 0, x, y, radius);
      gradient.addColorStop(0, `rgba(59, 130, 246, ${intensity})`);
      gradient.addColorStop(1, 'rgba(59, 130, 246, 0)');
      ctx.arc(x, y, radius, 0, 2 * Math.PI);
      ctx.fillStyle = gradient;
      ctx.fill();
    }

    function animate() {
      ctx.clearRect(0, 0, canvas.width, canvas.height);
      time += 0.03;

      edges.forEach(edge => {
        const fromNode = nodes.find(n => n.id === edge.from);
        const toNode = nodes.find(n => n.id === edge.to);
        ctx.beginPath();
        ctx.moveTo(fromNode.x, fromNode.y);
        ctx.lineTo(toNode.x, toNode.y);
        ctx.strokeStyle = 'rgba(255, 255, 255, 0.7)';
        ctx.lineWidth = 2;
        ctx.stroke();
      });

      nodes.forEach(node => {
        node.x += Math.sin(time + node.x * 0.01) * 0.3;
        node.y += Math.cos(time + node.y * 0.01) * 0.3;
        if (node.glow > 0) {
          drawGlow(node.x, node.y, 20, node.glow);
        }
        ctx.beginPath();
        ctx.arc(node.x, node.y, 12, 0, 2 * Math.PI);
        ctx.fillStyle = '#1e40af';
        ctx.fill();
        ctx.strokeStyle = '#ffffff';
        ctx.lineWidth = 1;
        ctx.stroke();
        ctx.font = '14px Inter';
        ctx.fillStyle = '#ffffff';
        ctx.fillText(node.label, node.x + 15, node.y - 10);
        if (hoveredNode === node) {
          ctx.fillStyle = 'rgba(255, 255, 255, 0.9)';
          ctx.fillText(node.metrics, node.x + 15, node.y + 10);
        }
      });

      requestAnimationFrame(animate);
    }
    animate();

    const handleMouseMove = e => {
      const rect = canvas.getBoundingClientRect();
      const mouseX = e.clientX - rect.left;
      const mouseY = e.clientY - rect.top;
      hoveredNode = null;
      nodes.forEach(node => {
        const dx = mouseX - node.x;
        const dy = mouseY - node.y;
        const distance = Math.sqrt(dx * dx + dy * dy);
        node.glow = distance < 20 ? 0.8 : 0;
        if (distance < 20) {
          hoveredNode = node;
        }
      });
    };

    canvas.addEventListener('mousemove', handleMouseMove);
    return () => {
      canvas.removeEventListener('mousemove', handleMouseMove);
    };
  }, []);

  return React.createElement(
    "canvas",
    {
      ref: canvasRef,
      className: "w-full max-w-lg mx-auto",
      role: "img",
      "aria-label": "Web3 animation depicting RDF and geospatial ontology connections"
    }
  );
};

const FilterForm = function() {
  const categories = [
    'datasets',
    'executionspecs',
    'hosts',
    'querysets',
    'reportsources',
    'reportspecs',
    'workloads'
  ];

  const jsonStructures = {
    datasets: {
      options: [
        'censusDSoriginal.json',
        'LUBM_1_0DSoriginal.json',
        'realworldDSoriginal.json',
        'scalability_100Koriginal.json',
        'scalability_100Moriginal.json',
        'scalability_10Koriginal.json',
        'scalability_10Moriginal.json',
        'scalability_1Moriginal.json',
        'scalability_500Moriginal.json',
        'syntheticDSoriginal.json',
        'syntheticPOIsDSoriginal.json'
      ],
      fields: {
        'censusDSoriginal.json': [
          { label: 'Class Name', name: 'classname', type: 'text', default: 'gr.uoa.di.rdf.Geographica3.rdf4j.impl.CensusDS' },
          { label: 'Name', name: 'name', type: 'text', default: 'census' },
          { label: 'Relative Base Dir', name: 'relativeBaseDir', type: 'text', default: '' },
          { label: 'Simple Geospatial Datasets (JSON)', name: 'simpleGeospatialDataSetList', type: 'textarea', default: JSON.stringify([{ name: 'census', relativeBaseDir: '', format: 'CSV' }], null, 2), validate: 'json' },
          { label: 'Triple Count', name: 'n', type: 'number', default: 0 }
        ],
        'LUBM_1_0DSoriginal.json': [
          { label: 'Class Name', name: 'classname', type: 'text', default: 'gr.uoa.di.rdf.Geographica3.runtime.dataset.impl.LUBMDS' },
          { label: 'Name', name: 'name', type: 'text', default: 'LUBM_1_0' },
          { label: 'Relative Base Dir', name: 'relativeBaseDir', type: 'text', default: '' },
          { label: 'Simple Geospatial Datasets (JSON)', name: 'simpleGeospatialDataSetList', type: 'textarea', default: JSON.stringify([{ name: 'lubm', relativeBaseDir: '', format: 'RDF' }], null, 2), validate: 'json' },
          { label: 'Triple Count', name: 'n', type: 'number', default: 0 }
        ],
        'realworldDSoriginal.json': [
          { label: 'Class Name', name: 'classname', type: 'text', default: 'gr.uoa.di.rdf.Geographica3.runtime.dataset.impl.SimpleGeospatialDS' },
          { label: 'Name', name: 'name', type: 'text', default: 'realworld' },
          { label: 'Relative Base Dir', name: 'relativeBaseDir', type: 'text', default: '' },
          { label: 'Simple Geospatial Datasets (JSON)', name: 'simpleGeospatialDataSetList', type: 'textarea', default: JSON.stringify([
            { name: 'corine', relativeBaseDir: '', format: 'RDF' },
            { name: 'dbpedia', relativeBaseDir: '', format: 'RDF' },
            { name: 'gag', relativeBaseDir: '', format: 'RDF' },
            { name: 'geonames', relativeBaseDir: '', format: 'RDF' },
            { name: 'hotspots', relativeBaseDir: '', format: 'RDF' },
            { name: 'linkedgeodata', relativeBaseDir: '', format: 'RDF' }
          ], null, 2), validate: 'json' },
          { label: 'Triple Count', name: 'n', type: 'number', default: 0 }
        ],
        'scalability_100Koriginal.json': [
          { label: 'Class Name', name: 'classname', type: 'text', default: 'gr.uoa.di.rdf.Geographica3.runtime.dataset.impl.SimpleGeospatialDS' },
          { label: 'Name', name: 'name', type: 'text', default: 'scalability_100K' },
          { label: 'Relative Base Dir', name: 'relativeBaseDir', type: 'text', default: '' },
          { label: 'Simple Geospatial Datasets (JSON)', name: 'simpleGeospatialDataSetList', type: 'textarea', default: JSON.stringify([{ name: 'scalability_100K', relativeBaseDir: '', format: 'RDF' }], null, 2), validate: 'json' },
          { label: 'Triple Count', name: 'n', type: 'number', default: 100000 }
        ],
        'scalability_100Moriginal.json': [
          { label: 'Class Name', name: 'classname', type: 'text', default: 'gr.uoa.di.rdf.Geographica3.runtime.dataset.impl.SimpleGeospatialDS' },
          { label: 'Name', name: 'name', type: 'text', default: 'scalability_100M' },
          { label: 'Relative Base Dir', name: 'relativeBaseDir', type: 'text', default: '' },
          { label: 'Simple Geospatial Datasets (JSON)', name: 'simpleGeospatialDataSetList', type: 'textarea', default: JSON.stringify([{ name: 'scalability_100M', relativeBaseDir: '', format: 'RDF' }], null, 2), validate: 'json' },
          { label: 'Triple Count', name: 'n', type: 'number', default: 100000000 }
        ],
        'scalability_10Koriginal.json': [
          { label: 'Class Name', name: 'classname', type: 'text', default: 'gr.uoa.di.rdf.Geographica3.runtime.dataset.impl.SimpleGeospatialDS' },
          { label: 'Name', name: 'name', type: 'text', default: 'scalability_10K' },
          { label: 'Relative Base Dir', name: 'relativeBaseDir', type: 'text', default: '' },
          { label: 'Simple Geospatial Datasets (JSON)', name: 'simpleGeospatialDataSetList', type: 'textarea', default: JSON.stringify([{ name: 'scalability_10K', relativeBaseDir: '', format: 'RDF' }], null, 2), validate: 'json' },
          { label: 'Triple Count', name: 'n', type: 'number', default: 10000 }
        ],
        'scalability_10Moriginal.json': [
          { label: 'Class Name', name: 'classname', type: 'text', default: 'gr.uoa.di.rdf.Geographica3.runtime.dataset.impl.SimpleGeospatialDS' },
          { label: 'Name', name: 'name', type: 'text', default: 'scalability_10M' },
          { label: 'Relative Base Dir', name: 'relativeBaseDir', type: 'text', default: '' },
          { label: 'Simple Geospatial Datasets (JSON)', name: 'simpleGeospatialDataSetList', type: 'textarea', default: JSON.stringify([{ name: 'scalability_10M', relativeBaseDir: '', format: 'RDF' }], null, 2), validate: 'json' },
          { label: 'Triple Count', name: 'n', type: 'number', default: 10000000 }
        ],
        'scalability_1Moriginal.json': [
          { label: 'Class Name', name: 'classname', type: 'text', default: 'gr.uoa.di.rdf.Geographica3.runtime.dataset.impl.SimpleGeospatialDS' },
          { label: 'Name', name: 'name', type: 'text', default: 'scalability_1M' },
          { label: 'Relative Base Dir', name: 'relativeBaseDir', type: 'text', default: '' },
          { label: 'Simple Geospatial Datasets (JSON)', name: 'simpleGeospatialDataSetList', type: 'textarea', default: JSON.stringify([{ name: 'scalability_1M', relativeBaseDir: '', format: 'RDF' }], null, 2), validate: 'json' },
          { label: 'Triple Count', name: 'n', type: 'number', default: 1000000 }
        ],
        'scalability_500Moriginal.json': [
          { label: 'Class Name', name: 'classname', type: 'text', default: 'gr.uoa.di.rdf.Geographica3.runtime.dataset.impl.SimpleGeospatialDS' },
          { label: 'Name', name: 'name', type: 'text', default: 'scalability_500M' },
          { label: 'Relative Base Dir', name: 'relativeBaseDir', type: 'text', default: '' },
          { label: 'Simple Geospatial Datasets (JSON)', name: 'simpleGeospatialDataSetList', type: 'textarea', default: JSON.stringify([{ name: 'scalability_500M', relativeBaseDir: '', format: 'RDF' }], null, 2), validate: 'json' },
          { label: 'Triple Count', name: 'n', type: 'number', default: 500000000 }
        ],
        'syntheticDSoriginal.json': [
          { label: 'Class Name', name: 'classname', type: 'text', default: 'gr.uoa.di.rdf.Geographica3.runtime.dataset.impl.SimpleGeospatialDS' },
          { label: 'Name', name: 'name', type: 'text', default: 'synthetic' },
          { label: 'Relative Base Dir', name: 'relativeBaseDir', type: 'text', default: '' },
          { label: 'Simple Geospatial Datasets (JSON)', name: 'simpleGeospatialDataSetList', type: 'textarea', default: JSON.stringify([{ name: 'synthetic', relativeBaseDir: '', format: 'RDF' }], null, 2), validate: 'json' },
          { label: 'Triple Count', name: 'n', type: 'number', default: 0 }
        ],
        'syntheticPOIsDSoriginal.json': [
          { label: 'Class Name', name: 'classname', type: 'text', default: 'gr.uoa.di.rdf.Geographica3.runtime.dataset.impl.SimpleGeospatialDS' },
          { label: 'Name', name: 'name', type: 'text', default: 'syntheticPOIs' },
          { label: 'Relative Base Dir', name: 'relativeBaseDir', type: 'text', default: '' },
          { label: 'Simple Geospatial Datasets (JSON)', name: 'simpleGeospatialDataSetList', type: 'textarea', default: JSON.stringify([{ name: 'syntheticPOIs', relativeBaseDir: '', format: 'RDF' }], null, 2), validate: 'json' },
          { label: 'Triple Count', name: 'n', type: 'number', default: 0 }
        ]
      }
    },
    executionspecs: {
      options: [
        'LUBM_1_0ESoriginal.json',
        'macroESoriginal.json',
        'microESoriginal_fast.json',
        'microESoriginal.json',
        'scalabilityESoriginal.json'
      ],
      fields: {
        'LUBM_1_0ESoriginal.json': [
          { label: 'Class Name', name: 'classname', type: 'text', default: 'gr.uoa.di.rdf.Geographica3.runtime.executionspecs.impl.SimpleES' },
          { label: 'Execution Type Repetitions (JSON)', name: 'execTypeReps', type: 'textarea', default: JSON.stringify({ WARM: 3, COLD: 3 }, null, 2), validate: 'json' },
          { label: 'Max Duration Per Query (s)', name: 'maxDurationSecsPerQueryRep', type: 'number', default: 1800 },
          { label: 'Max Total Duration (s)', name: 'maxDurationSecs', type: 'number', default: 3600 },
          { label: 'Action', name: 'action', type: 'select', options: ['RUN', 'TEST'], default: 'RUN' },
          { label: 'Average Function', name: 'avgFunc', type: 'select', options: ['QUERY_MEDIAN', 'QUERYSET_MEAN'], default: 'QUERY_MEDIAN' },
          { label: 'On Cold Failure', name: 'onColdFailure', type: 'select', options: ['SKIP_REMAINING_ALL_QUERY_EXECUTIONS', 'EXCLUDE_AND_PROCEED'], default: 'SKIP_REMAINING_ALL_QUERY_EXECUTIONS' },
          { label: 'Clear Cache Delay (ms)', name: 'clearCacheDelaymSecs', type: 'number', default: 5000 }
        ],
        'macroESoriginal.json': [
          { label: 'Class Name', name: 'classname', type: 'text', default: 'gr.uoa.di.rdf.Geographica3.runtime.executionspecs.impl.SimpleES' },
          { label: 'Execution Type Repetitions (JSON)', name: 'execTypeReps', type: 'textarea', default: JSON.stringify({ COLD_CONTINUOUS: -1 }, null, 2), validate: 'json' },
          { label: 'Max Duration Per Query (s)', name: 'maxDurationSecsPerQueryRep', type: 'number', default: 3610 },
          { label: 'Max Total Duration (s)', name: 'maxDurationSecs', type: 'number', default: 3600 },
          { label: 'Action', name: 'action', type: 'select', options: ['RUN', 'TEST'], default: 'RUN' },
          { label: 'Average Function', name: 'avgFunc', type: 'select', options: ['QUERY_MEDIAN', 'QUERYSET_MEAN'], default: 'QUERYSET_MEAN' },
          { label: 'On Cold Failure', name: 'onColdFailure', type: 'select', options: ['SKIP_REMAINING_ALL_QUERY_EXECUTIONS', 'EXCLUDE_AND_PROCEED'], default: 'EXCLUDE_AND_PROCEED' },
          { label: 'Clear Cache Delay (ms)', name: 'clearCacheDelaymSecs', type: 'number', default: 5000 }
        ],
        'microESoriginal_fast.json': [
          { label: 'Class Name', name: 'classname', type: 'text', default: 'gr.uoa.di.rdf.Geographica3.runtime.executionspecs.impl.SimpleES' },
          { label: 'Execution Type Repetitions (JSON)', name: 'execTypeReps', type: 'textarea', default: JSON.stringify({ COLD: 1, WARM: 1 }, null, 2), validate: 'json' },
          { label: 'Max Duration Per Query (s)', name: 'maxDurationSecsPerQueryRep', type: 'number', default: 180 },
          { label: 'Max Total Duration (s)', name: 'maxDurationSecs', type: 'number', default: 360 },
          { label: 'Action', name: 'action', type: 'select', options: ['RUN', 'TEST'], default: 'RUN' },
          { label: 'Average Function', name: 'avgFunc', type: 'select', options: ['QUERY_MEDIAN', 'QUERYSET_MEAN'], default: 'QUERY_MEDIAN' },
          { label: 'On Cold Failure', name: 'onColdFailure', type: 'select', options: ['SKIP_REMAINING_ALL_QUERY_EXECUTIONS', 'EXCLUDE_AND_PROCEED'], default: 'SKIP_REMAINING_ALL_QUERY_EXECUTIONS' },
          { label: 'Clear Cache Delay (ms)', name: 'clearCacheDelaymSecs', type: 'number', default: 2000 }
        ],
        'microESoriginal.json': [
          { label: 'Class Name', name: 'classname', type: 'text', default: 'gr.uoa.di.rdf.Geographica3.runtime.executionspecs.impl.SimpleES' },
          { label: 'Execution Type Repetitions (JSON)', name: 'execTypeReps', type: 'textarea', default: JSON.stringify({ COLD: 3, WARM: 3 }, null, 2), validate: 'json' },
          { label: 'Max Duration Per Query (s)', name: 'maxDurationSecsPerQueryRep', type: 'number', default: 1800 },
          { label: 'Max Total Duration (s)', name: 'maxDurationSecs', type: 'number', default: 3600 },
          { label: 'Action', name: 'action', type: 'select', options: ['RUN', 'TEST'], default: 'RUN' },
          { label: 'Average Function', name: 'avgFunc', type: 'select', options: ['QUERY_MEDIAN', 'QUERYSET_MEAN'], default: 'QUERY_MEDIAN' },
          { label: 'On Cold Failure', name: 'onColdFailure', type: 'select', options: ['SKIP_REMAINING_ALL_QUERY_EXECUTIONS', 'EXCLUDE_AND_PROCEED'], default: 'SKIP_REMAINING_ALL_QUERY_EXECUTIONS' },
          { label: 'Clear Cache Delay (ms)', name: 'clearCacheDelaymSecs', type: 'number', default: 5000 }
        ],
        'scalabilityESoriginal.json': [
          { label: 'Class Name', name: 'classname', type: 'text', default: 'gr.uoa.di.rdf.Geographica3.runtime.executionspecs.impl.SimpleES' },
          { label: 'Execution Type Repetitions (JSON)', name: 'execTypeReps', type: 'textarea', default: JSON.stringify({ COLD: 3, WARM: 3 }, null, 2), validate: 'json' },
          { label: 'Max Duration Per Query (s)', name: 'maxDurationSecsPerQueryRep', type: 'number', default: 86400 },
          { label: 'Max Total Duration (s)', name: 'maxDurationSecs', type: 'number', default: 604800 },
          { label: 'Action', name: 'action', type: 'select', options: ['RUN', 'TEST'], default: 'RUN' },
          { label: 'Average Function', name: 'avgFunc', type: 'select', options: ['QUERY_MEDIAN', 'QUERYSET_MEAN'], default: 'QUERY_MEDIAN' },
          { label: 'On Cold Failure', name: 'onColdFailure', type: 'select', options: ['SKIP_REMAINING_ALL_QUERY_EXECUTIONS', 'EXCLUDE_AND_PROCEED'], default: 'SKIP_REMAINING_ALL_QUERY_EXECUTIONS' },
          { label: 'Clear Cache Delay (ms)', name: 'clearCacheDelaymSecs', type: 'number', default: 5000 }
        ]
      }
    },
    hosts: {
      options: [
        'nuc8i7behHOSToriginal.json',
        'teleios3HOSToriginal.json',
        'tioa-paviliondv7HOSToriginal.json',
        'ubuntu_vma_tioaHOSToriginal.json',
        'win10_workHOSToriginal.json'
      ],
      fields: {
        'nuc8i7behHOSToriginal.json': [
          { label: 'Class Name', name: 'classname', type: 'text', default: 'gr.uoa.di.rdf.Geographica3.runtime.host.impl.HostImpl' },
          { label: 'Name', name: 'name', type: 'text', default: 'nuc8i7beh' },
          { label: 'OS', name: 'os', type: 'text', default: 'Ubuntu 20.04' },
          { label: 'Memory (GB)', name: 'memoryGB', type: 'number', default: 16 },
          { label: 'Cores', name: 'cores', type: 'number', default: 4 },
          { label: 'Disk Size (GB)', name: 'diskSizeGB', type: 'number', default: 512 }
        ],
        'teleios3HOSToriginal.json': [
          { label: 'Class Name', name: 'classname', type: 'text', default: 'gr.uoa.di.rdf.Geographica3.runtime.host.impl.HostImpl' },
          { label: 'Name', name: 'name', type: 'text', default: 'teleios3' },
          { label: 'OS', name: 'os', type: 'text', default: 'Ubuntu 18.04' },
          { label: 'Memory (GB)', name: 'memoryGB', type: 'number', default: 32 },
          { label: 'Cores', name: 'cores', type: 'number', default: 8 },
          { label: 'Disk Size (GB)', name: 'diskSizeGB', type: 'number', default: 1000 }
        ],
        'tioa-paviliondv7HOSToriginal.json': [
          { label: 'Class Name', name: 'classname', type: 'text', default: 'gr.uoa.di.rdf.Geographica3.runtime.host.impl.HostImpl' },
          { label: 'Name', name: 'name', type: 'text', default: 'tioa-paviliondv7' },
          { label: 'OS', name: 'os', type: 'text', default: 'Windows 10' },
          { label: 'Memory (GB)', name: 'memoryGB', type: 'number', default: 16 },
          { label: 'Cores', name: 'cores', type: 'number', default: 4 },
          { label: 'Disk Size (GB)', name: 'diskSizeGB', type: 'number', default: 500 }
        ],
        'ubuntu_vma_tioaHOSToriginal.json': [
          { label: 'Class Name', name: 'classname', type: 'text', default: 'gr.uoa.di.rdf.Geographica3.runtime.host.impl.HostImpl' },
          { label: 'Name', name: 'name', type: 'text', default: 'ubuntu_vma_tioa' },
          { label: 'OS', name: 'os', type: 'text', default: 'Ubuntu 20.04' },
          { label: 'Memory (GB)', name: 'memoryGB', type: 'number', default: 8 },
          { label: 'Cores', name: 'cores', type: 'number', default: 2 },
          { label: 'Disk Size (GB)', name: 'diskSizeGB', type: 'number', default: 256 }
        ],
        'win10_workHOSToriginal.json': [
          { label: 'Class Name', name: 'classname', type: 'text', default: 'gr.uoa.di.rdf.Geographica3.runtime.host.impl.HostImpl' },
          { label: 'Name', name: 'name', type: 'text', default: 'win10_work' },
          { label: 'OS', name: 'os', type: 'text', default: 'Windows 10' },
          { label: 'Memory (GB)', name: 'memoryGB', type: 'number', default: 32 },
          { label: 'Cores', name: 'cores', type: 'number', default: 8 },
          { label: 'Disk Size (GB)', name: 'diskSizeGB', type: 'number', default: 1000 }
        ]
      }
    },
    querysets: {
      options: [
        'rwmacromapsearchQSoriginal.json',
        'rwmacrorapidmappingQSoriginal.json',
        'scalabilityFuncQSoriginal.json',
        'scalabilityPredQSoriginal.json',
        'syntheticQSoriginal.json'
      ],
      fields: {
        'rwmacromapsearchQSoriginal.json': [
          { label: 'Class Name', name: 'classname', type: 'text', default: 'gr.uoa.di.rdf.Geographica3.runtime.querysets.complex.impl.MacroMapSearchQS' },
          { label: 'Name', name: 'name', type: 'text', default: 'mapsearch' },
          { label: 'Relative Base Dir', name: 'relativeBaseDir', type: 'text', default: '' },
          { label: 'Has Predicate Queries', name: 'hasPredicateQueriesAlso', type: 'select', options: ['true', 'false'], default: 'false' },
          { label: 'Map Queries (JSON)', name: 'mapQueries', type: 'textarea', default: JSON.stringify([{ label: 'q1', text: 'SELECT ...', usePredicate: false, expectedResults: 0 }], null, 2), validate: 'json' },
          { label: 'Namespace Prefixes (JSON)', name: 'mapUsefulNamespacePrefixes', type: 'textarea', default: JSON.stringify({ geosparql: 'http://www.opengis.net/ont/geosparql#', geo: 'http://www.geonames.org/ontology#' }, null, 2), validate: 'json' },
          { label: 'Toponym Template Name', name: 'toponymTemplateName', type: 'text', default: '<TOPONYME>' },
          { label: 'Rectangle Template Name', name: 'rectangleTemplateName', type: 'text', default: '<RECTANGLE_LITERAL>' }
        ],
        'rwmacrorapidmappingQSoriginal.json': [
          { label: 'Class Name', name: 'classname', type: 'text', default: 'gr.uoa.di.rdf.Geographica3.runtime.querysets.complex.impl.MacroRapidMappingQS' },
          { label: 'Name', name: 'name', type: 'text', default: 'rapidmapping' },
          { label: 'Relative Base Dir', name: 'relativeBaseDir', type: 'text', default: '' },
          { label: 'Has Predicate Queries', name: 'hasPredicateQueriesAlso', type: 'select', options: ['true', 'false'], default: 'false' },
          { label: 'Map Queries (JSON)', name: 'mapQueries', type: 'textarea', default: JSON.stringify([{ label: 'q1', text: 'SELECT ...', usePredicate: false, expectedResults: 0 }], null, 2), validate: 'json' },
          { label: 'Namespace Prefixes (JSON)', name: 'mapUsefulNamespacePrefixes', type: 'textarea', default: JSON.stringify({ geosparql: 'http://www.opengis.net/ont/geosparql#', geo: 'http://www.geonames.org/ontology#' }, null, 2), validate: 'json' },
          { label: 'Timestamp Template Name', name: 'timestampTemplateName', type: 'text', default: '<TIMESTAMP>' },
          { label: 'Polygon Template Name', name: 'polygonTemplateName', type: 'text', default: '<GIVEN_POLYGON_IN_WKT>' }
        ],
        'scalabilityFuncQSoriginal.json': [
          { label: 'Class Name', name: 'classname', type: 'text', default: 'gr.uoa.di.rdf.Geographica3.runtime.querysets.complex.impl.StaticTempParamplanarQS' },
          { label: 'Name', name: 'name', type: 'text', default: 'scalabilityFunc' },
          { label: 'Relative Base Dir', name: 'relativeBaseDir', type: 'text', default: '' },
          { label: 'Has Predicate Queries', name: 'hasPredicateQueriesAlso', type: 'select', options: ['true', 'false'], default: 'false' },
          { label: 'Map Queries (JSON)', name: 'mapQueries', type: 'textarea', default: JSON.stringify([
            { label: 'q1', text: 'SELECT ?s ?o WHERE { ?s <http://www.opengis.net/ont/geosparql#asWKT> ?o . FILTER( <http://www.opengis.net/def/function/geosparql/sfIntersects> (?o, "POLYGON((23.708496093749996 37.95719224376526,22.906494140625 40.659805938378526,11.524658203125002 48.16425348854739,-0.1181030273437499 51.49506473014367,-3.2189941406250004 55.92766341247031,-5.940856933593749 54.59116279530599,-3.1668090820312504 51.47967237816337,23.708496093749996 37.95719224376526))"^^<http://www.opengis.net/ont/geosparql#wktLiteral>) ) }', usePredicate: false, expectedResults: 0 }
          ], null, 2), validate: 'json' },
          { label: 'Namespace Prefixes (JSON)', name: 'mapUsefulNamespacePrefixes', type: 'textarea', default: JSON.stringify({ geosparql: 'http://www.opengis.net/ont/geosparql#' }, null, 2), validate: 'json' },
          { label: 'Template Params (JSON)', name: 'mapTemplateParams', type: 'textarea', default: JSON.stringify({
            GIVEN_SPATIAL_LITERAL: '"POLYGON((23.708496093749996 37.95719224376526,22.906494140625 40.659805938378526,11.524658203125002 48.16425348854739,-0.1181030273437499 51.49506473014367,-3.2189941406250004 55.92766341247031,-5.940856933593749 54.59116279530599,-3.1668090820312504 51.47967237816337,23.708496093749996 37.95719224376526))"^^<http://www.opengis.net/ont/geosparql#wktLiteral>',
            FUNCTION: 'sfIntersects'
          }, null, 2), validate: 'json' }
        ],
        'scalabilityPredQSoriginal.json': [
          { label: 'Class Name', name: 'classname', type: 'text', default: 'gr.uoa.di.rdf.Geographica3.runtime.querysets.complex.impl.StaticTempParamQS' },
          { label: 'Name', name: 'name', type: 'text', default: 'scalabilityPred' },
          { label: 'Relative Base Dir', name: 'relativeBaseDir', type: 'text', default: '' },
          { label: 'Has Predicate Queries', name: 'hasPredicateQueriesAlso', type: 'select', options: ['true', 'false'], default: 'true' },
          { label: 'Map Queries (JSON)', name: 'mapQueries', type: 'textarea', default: JSON.stringify([
            { label: 'q1', text: 'SELECT ?s ?o WHERE { ?s <http://www.opengis.net/ont/geosparql#asWKT> ?o . ?s ?p "POLYGON((23.708496093749996 37.95719224376526,22.906494140625 40.659805938378526,11.524658203125002 48.16425348854739,-0.1181030273437499 51.49506473014367,-3.2189941406250004 55.92766341247031,-5.940856933593749 54.59116279530599,-3.1668090820312504 51.47967237816337,23.708496093749996 37.95719224376526))"^^<http://www.opengis.net/ont/geosparql#wktLiteral> . }', usePredicate: true, expectedResults: 0 }
          ], null, 2), validate: 'json' },
          { label: 'Namespace Prefixes (JSON)', name: 'mapUsefulNamespacePrefixes', type: 'textarea', default: JSON.stringify({ geosparql: 'http://www.opengis.net/ont/geosparql#' }, null, 2), validate: 'json' },
          { label: 'Template Params (JSON)', name: 'mapTemplateParams', type: 'textarea', default: JSON.stringify({
            GIVEN_SPATIAL_LITERAL: '"POLYGON((23.708496093749996 37.95719224376526,22.906494140625 40.659805938378526,11.524658203125002 48.16425348854739,-0.1181030273437499 51.49506473014367,-3.2189941406250004 55.92766341247031,-5.940856933593749 54.59116279530599,-3.1668090820312504 51.47967237816337,23.708496093749996 37.95719224376526))"^^<http://www.opengis.net/ont/geosparql#wktLiteral>',
            FUNCTION: 'sfIntersects'
          }, null, 2), validate: 'json' }
        ],
        'syntheticQSoriginal.json': [
          { label: 'Class Name', name: 'classname', type: 'text', default: 'gr.uoa.di.rdf.Geographica3.runtime.querysets.complex.impl.StaticTempParamQS' },
          { label: 'Name', name: 'name', type: 'text', default: 'synthetic' },
          { label: 'Relative Base Dir', name: 'relativeBaseDir', type: 'text', default: '' },
          { label: 'Has Predicate Queries', name: 'hasPredicateQueriesAlso', type: 'select', options: ['true', 'false'], default: 'false' },
          { label: 'Map Queries (JSON)', name: 'mapQueries', type: 'textarea', default: JSON.stringify([{ label: 'q1', text: 'SELECT ...', usePredicate: false, expectedResults: 0 }], null, 2), validate: 'json' },
          { label: 'Namespace Prefixes (JSON)', name: 'mapUsefulNamespacePrefixes', type: 'textarea', default: JSON.stringify({ geosparql: 'http://www.opengis.net/ont/geosparql#' }, null, 2), validate: 'json' }
        ]
      }
    },
    reportsources: {
      options: [
        'h2EmbeddedRepSrcoriginal.json',
        'nuc8i7behHOSToriginal.json',
        'ubuntu_vma_tioaRepSrcoriginal.json'
      ],
      fields: {
        'h2EmbeddedRepSrcoriginal.json': [
          { label: 'Class Name', name: 'classname', type: 'text', default: 'gr.uoa.di.rdf.Geographica3.runtime.reportsource.impl.H2EmbeddedRepSrc' },
          { label: 'Driver', name: 'driver', type: 'text', default: 'h2' },
          { label: 'Relative Base Directory', name: 'relativeBaseDir', type: 'text', default: '../scripts/h2embeddedreportsource' },
          { label: 'Database', name: 'database', type: 'text', default: 'geordfbench' },
          { label: 'User', name: 'user', type: 'text', default: 'sa' },
          { label: 'Password', name: 'password', type: 'text', default: 'fwq93#2*%$%@#$dRTw' }
        ],
        'nuc8i7behHOSToriginal.json': [
          { label: 'Class Name', name: 'classname', type: 'text', default: 'gr.uoa.di.rdf.Geographica3.runtime.reportsource.impl.HostBasedRepSrc' },
          { label: 'Driver', name: 'driver', type: 'text', default: 'postgresql' },
          { label: 'Host Name', name: 'hostname', type: 'text', default: '192.168.1.44' },
          { label: 'Alt. Host Name', name: 'althostname', type: 'text', default: 'localhost' },
          { label: 'Port', name: 'port', type: 'number', default: 5432 },
          { label: 'Database', name: 'database', type: 'text', default: 'geordfbench' },
          { label: 'User', name: 'user', type: 'text', default: 'sa' },
          { label: 'Password', name: 'password', type: 'text', default: 'fwq93#2*%$%@#$dRTw' }
        ],
        'ubuntu_vma_tioaRepSrcoriginal.json': [
          { label: 'Class Name', name: 'classname', type: 'text', default: 'gr.uoa.di.rdf.Geographica3.runtime.reportsource.impl.PostgreSQLRepSrc' },
          { label: 'Driver', name: 'driver', type: 'text', default: 'postgresql' },
          { label: 'Host Name', name: 'hostname', type: 'text', default: '10.0.2.15' },
          { label: 'Alt. Host Name', name: 'althostname', type: 'text', default: 'localhost' },
          { label: 'Port', name: 'port', type: 'number', default: 5432 },
          { label: 'Database', name: 'database', type: 'text', default: 'geordfbench' },
          { label: 'User', name: 'user', type: 'text', default: 'geordfbench' },
          { label: 'Password', name: 'password', type: 'text', default: 'geordfbench' }
        ]
      }
    },
    reportspecs: {
      options: ['simplereportspec_original.json'],
      fields: {
        'simplereportspec_original.json': [
          { label: 'Class Name', name: 'classname', type: 'text', default: 'gr.uoa.di.rdf.Geographica3.runtime.reportspecs.impl.SimpleReportSpec' },
          { label: 'No Query Result to Report', name: 'noQueryResultToReport', type: 'number', default: 3 }
        ]
      }
    },
    workloads: {
      options: [
        'rwmacromapsearchWLoriginal.json',
        'rwmacrorapidmappingWLoriginal.json',
        'scalabilityFunc500M_WLoriginal.json',
        'scalabilityPred500M_WLoriginal.json'
      ],
      fields: {
        'rwmacromapsearchWLoriginal.json': [
          { label: 'Class Name', name: 'classname', type: 'text', default: 'gr.uoa.di.rdf.Geographica3.runtime.workloadspecs.impl.SimpleGeospatialWL' },
          { label: 'Name', name: 'name', type: 'text', default: 'RWMacroMapSearch' },
          { label: 'Relative Base Dir', name: 'relativeBaseDir', type: 'text', default: '' },
          { label: 'Dataset Class Name', name: 'geospatialDataset.classname', type: 'text', default: 'gr.uoa.di.rdf.Geographica3.runtime.dataset.impl.SimpleGeospatialDS' },
          { label: 'Dataset Name', name: 'geospatialDataset.name', type: 'text', default: 'realworld' },
          { label: 'Dataset Simple Geospatial Datasets (JSON)', name: 'geospatialDataset.simpleGeospatialDataSetList', type: 'textarea', default: JSON.stringify([
            { name: 'corine', relativeBaseDir: '', format: 'RDF' },
            { name: 'dbpedia', relativeBaseDir: '', format: 'RDF' },
            { name: 'gag', relativeBaseDir: '', format: 'RDF' },
            { name: 'geonames', relativeBaseDir: '', format: 'RDF' },
            { name: 'hotspots', relativeBaseDir: '', format: 'RDF' },
            { name: 'linkedgeodata', relativeBaseDir: '', format: 'RDF' }
          ], null, 2), validate: 'json' },
          { label: 'Dataset Triple Count', name: 'geospatialDataset.n', type: 'number', default: 0 },
          { label: 'Queryset Class Name', name: 'geospatialQueryset.classname', type: 'text', default: 'gr.uoa.di.rdf.Geographica3.runtime.querysets.complex.impl.MacroMapSearchQS' },
          { label: 'Queryset Name', name: 'geospatialQueryset.name', type: 'text', default: 'mapsearch' },
          { label: 'Queryset Has Predicate Queries', name: 'geospatialQueryset.hasPredicateQueriesAlso', type: 'select', options: ['true', 'false'], default: 'false' },
          { label: 'Queryset Map Queries (JSON)', name: 'geospatialQueryset.mapQueries', type: 'textarea', default: JSON.stringify([{ label: 'q1', text: 'SELECT ...', usePredicate: false, expectedResults: 0 }], null, 2), validate: 'json' },
          { label: 'Queryset Namespace Prefixes (JSON)', name: 'geospatialQueryset.mapUsefulNamespacePrefixes', type: 'textarea', default: JSON.stringify({ geosparql: 'http://www.opengis.net/ont/geosparql#', geo: 'http://www.geonames.org/ontology#' }, null, 2), validate: 'json' },
          { label: 'Queryset Toponym Template Name', name: 'geospatialQueryset.toponymTemplateName', type: 'text', default: '<TOPONYME>' },
          { label: 'Queryset Rectangle Template Name', name: 'geospatialQueryset.rectangleTemplateName', type: 'text', default: '<RECTANGLE_LITERAL>' },
          { label: 'Execution Spec Class Name', name: 'geospatialQueryset.executionSpec.classname', type: 'text', default: 'gr.uoa.di.rdf.Geographica3.runtime.executionspecs.impl.SimpleES' },
          { label: 'Execution Spec Repetitions (JSON)', name: 'geospatialQueryset.executionSpec.execTypeReps', type: 'textarea', default: JSON.stringify({ COLD_CONTINUOUS: -1 }, null, 2), validate: 'json' },
          { label: 'Execution Spec Max Duration Per Query (s)', name: 'geospatialQueryset.executionSpec.maxDurationSecsPerQueryRep', type: 'number', default: 3610 },
          { label: 'Execution Spec Max Total Duration (s)', name: 'geospatialQueryset.executionSpec.maxDurationSecs', type: 'number', default: 3600 },
          { label: 'Execution Spec Action', name: 'geospatialQueryset.executionSpec.action', type: 'select', options: ['RUN', 'TEST'], default: 'RUN' },
          { label: 'Execution Spec Average Function', name: 'geospatialQueryset.executionSpec.avgFunc', type: 'select', options: ['QUERY_MEDIAN', 'QUERYSET_MEAN'], default: 'QUERYSET_MEAN' },
          { label: 'Execution Spec On Cold Failure', name: 'geospatialQueryset.executionSpec.onColdFailure', type: 'select', options: ['SKIP_REMAINING_ALL_QUERY_EXECUTIONS', 'EXCLUDE_AND_PROCEED'], default: 'EXCLUDE_AND_PROCEED' },
          { label: 'Execution Spec Clear Cache Delay (ms)', name: 'geospatialQueryset.executionSpec.clearCacheDelaymSecs', type: 'number', default: 5000 }
        ],
        'rwmacrorapidmappingWLoriginal.json': [
          { label: 'Class Name', name: 'classname', type: 'text', default: 'gr.uoa.di.rdf.Geographica3.runtime.workloadspecs.impl.SimpleGeospatialWL' },
          { label: 'Name', name: 'name', type: 'text', default: 'RWMacroRapidMapping' },
          { label: 'Relative Base Dir', name: 'relativeBaseDir', type: 'text', default: '' },
          { label: 'Dataset Class Name', name: 'geospatialDataset.classname', type: 'text', default: 'gr.uoa.di.rdf.Geographica3.runtime.dataset.impl.SimpleGeospatialDS' },
          { label: 'Dataset Name', name: 'geospatialDataset.name', type: 'text', default: 'realworld' },
          { label: 'Dataset Simple Geospatial Datasets (JSON)', name: 'geospatialDataset.simpleGeospatialDataSetList', type: 'textarea', default: JSON.stringify([
            { name: 'corine', relativeBaseDir: '', format: 'RDF' },
            { name: 'dbpedia', relativeBaseDir: '', format: 'RDF' },
            { name: 'gag', relativeBaseDir: '', format: 'RDF' },
            { name: 'geonames', relativeBaseDir: '', format: 'RDF' },
            { name: 'hotspots', relativeBaseDir: '', format: 'RDF' },
            { name: 'linkedgeodata', relativeBaseDir: '', format: 'RDF' }
          ], null, 2), validate: 'json' },
          { label: 'Dataset Triple Count', name: 'geospatialDataset.n', type: 'number', default: 0 },
          { label: 'Queryset Class Name', name: 'geospatialQueryset.classname', type: 'text', default: 'gr.uoa.di.rdf.Geographica3.runtime.querysets.complex.impl.MacroRapidMappingQS' },
          { label: 'Queryset Name', name: 'geospatialQueryset.name', type: 'text', default: 'rapidmapping' },
          { label: 'Queryset Has Predicate Queries', name: 'geospatialQueryset.hasPredicateQueriesAlso', type: 'select', options: ['true', 'false'], default: 'false' },
          { label: 'Queryset Map Queries (JSON)', name: 'geospatialQueryset.mapQueries', type: 'textarea', default: JSON.stringify([{ label: 'q1', text: 'SELECT ...', usePredicate: false, expectedResults: 0 }], null, 2), validate: 'json' },
          { label: 'Queryset Namespace Prefixes (JSON)', name: 'geospatialQueryset.mapUsefulNamespacePrefixes', type: 'textarea', default: JSON.stringify({ geosparql: 'http://www.opengis.net/ont/geosparql#', geo: 'http://www.geonames.org/ontology#' }, null, 2), validate: 'json' },
          { label: 'Queryset Timestamp Template Name', name: 'geospatialQueryset.timestampTemplateName', type: 'text', default: '<TIMESTAMP>' },
          { label: 'Queryset Polygon Template Name', name: 'geospatialQueryset.polygonTemplateName', type: 'text', default: '<GIVEN_POLYGON_IN_WKT>' },
          { label: 'Execution Spec Class Name', name: 'geospatialQueryset.executionSpec.classname', type: 'text', default: 'gr.uoa.di.rdf.Geographica3.runtime.executionspecs.impl.SimpleES' },
          { label: 'Execution Spec Repetitions (JSON)', name: 'geospatialQueryset.executionSpec.execTypeReps', type: 'textarea', default: JSON.stringify({ COLD_CONTINUOUS: -1 }, null, 2), validate: 'json' },
          { label: 'Execution Spec Max Duration Per Query (s)', name: 'geospatialQueryset.executionSpec.maxDurationSecsPerQueryRep', type: 'number', default: 3610 },
          { label: 'Execution Spec Max Total Duration (s)', name: 'geospatialQueryset.executionSpec.maxDurationSecs', type: 'number', default: 3600 },
          { label: 'Execution Spec Action', name: 'geospatialQueryset.executionSpec.action', type: 'select', options: ['RUN', 'TEST'], default: 'RUN' },
          { label: 'Execution Spec Average Function', name: 'geospatialQueryset.executionSpec.avgFunc', type: 'select', options: ['QUERY_MEDIAN', 'QUERYSET_MEAN'], default: 'QUERYSET_MEAN' },
          { label: 'Execution Spec On Cold Failure', name: 'geospatialQueryset.executionSpec.onColdFailure', type: 'select', options: ['SKIP_REMAINING_ALL_QUERY_EXECUTIONS', 'EXCLUDE_AND_PROCEED'], default: 'EXCLUDE_AND_PROCEED' },
          { label: 'Execution Spec Clear Cache Delay (ms)', name: 'geospatialQueryset.executionSpec.clearCacheDelaymSecs', type: 'number', default: 5000 }
        ],
        'scalabilityFunc500M_WLoriginal.json': [
          { label: 'Class Name', name: 'classname', type: 'text', default: 'gr.uoa.di.rdf.Geographica3.runtime.workloadspecs.impl.SimpleGeospatialWL' },
          { label: 'Name', name: 'name', type: 'text', default: 'ScalabilityFunc' },
          { label: 'Relative Base Dir', name: 'relativeBaseDir', type: 'text', default: '' },
          { label: 'Dataset Class Name', name: 'geospatialDataset.classname', type: 'text', default: 'gr.uoa.di.rdf.Geographica3.runtime.dataset.impl.SimpleGeospatialDS' },
          { label: 'Dataset Name', name: 'geospatialDataset.name', type: 'text', default: 'scalability_500M' },
          { label: 'Dataset Simple Geospatial Datasets (JSON)', name: 'geospatialDataset.simpleGeospatialDataSetList', type: 'textarea', default: JSON.stringify([{ name: 'scalability_500M', relativeBaseDir: '', format: 'RDF' }], null, 2), validate: 'json' },
          { label: 'Dataset Triple Count', name: 'geospatialDataset.n', type: 'number', default: 500000000 },
          { label: 'Queryset Class Name', name: 'geospatialQueryset.classname', type: 'text', default: 'gr.uoa.di.rdf.Geographica3.runtime.querysets.complex.impl.StaticTempParamQS' },
          { label: 'Queryset Name', name: 'geospatialQueryset.name', type: 'text', default: 'scalabilityFunc' },
          { label: 'Queryset Has Predicate Queries', name: 'geospatialQueryset.hasPredicateQueriesAlso', type: 'select', options: ['true', 'false'], default: 'false' },
          { label: 'Queryset Map Queries (JSON)', name: 'geospatialQueryset.mapQueries', type: 'textarea', default: JSON.stringify([
            { label: 'q1', text: 'SELECT ?s ?o WHERE { ?s <http://www.opengis.net/ont/geosparql#asWKT> ?o . FILTER( <http://www.opengis.net/def/function/geosparql/sfIntersects> (?o, "POLYGON((23.708496093749996 37.95719224376526,22.906494140625 40.659805938378526,11.524658203125002 48.16425348854739,-0.1181030273437499 51.49506473014367,-3.2189941406250004 55.92766341247031,-5.940856933593749 54.59116279530599,-3.1668090820312504 51.47967237816337,23.708496093749996 37.95719224376526))"^^<http://www.opengis.net/ont/geosparql#wktLiteral>) ) }', usePredicate: false, expectedResults: 0 }
          ], null, 2), validate: 'json' },
          { label: 'Queryset Namespace Prefixes (JSON)', name: 'geospatialQueryset.mapUsefulNamespacePrefixes', type: 'textarea', default: JSON.stringify({ geosparql: 'http://www.opengis.net/ont/geosparql#' }, null, 2), validate: 'json' },
          { label: 'Queryset Template Params (JSON)', name: 'geospatialQueryset.mapTemplateParams', type: 'textarea', default: JSON.stringify({
            GIVEN_SPATIAL_LITERAL: '"POLYGON((23.708496093749996 37.95719224376526,22.906494140625 40.659805938378526,11.524658203125002 48.16425348854739,-0.1181030273437499 51.49506473014367,-3.2189941406250004 55.92766341247031,-5.940856933593749 54.59116279530599,-3.1668090820312504 51.47967237816337,23.708496093749996 37.95719224376526))"^^<http://www.opengis.net/ont/geosparql#wktLiteral>',
            FUNCTION: 'sfIntersects'
          }, null, 2), validate: 'json' },
          { label: 'Execution Spec Class Name', name: 'geospatialQueryset.executionSpec.classname', type: 'text', default: 'gr.uoa.di.rdf.Geographica3.runtime.executionspecs.impl.SimpleES' },
          { label: 'Execution Spec Repetitions (JSON)', name: 'geospatialQueryset.executionSpec.execTypeReps', type: 'textarea', default: JSON.stringify({ COLD: 3, WARM: 3 }, null, 2), validate: 'json' },
          { label: 'Execution Spec Max Duration Per Query (s)', name: 'geospatialQueryset.executionSpec.maxDurationSecsPerQueryRep', type: 'number', default: 86400 },
          { label: 'Execution Spec Max Total Duration (s)', name: 'geospatialQueryset.executionSpec.maxDurationSecs', type: 'number', default: 604800 },
          { label: 'Execution Spec Action', name: 'geospatialQueryset.executionSpec.action', type: 'select', options: ['RUN', 'TEST'], default: 'RUN' },
          { label: 'Execution Spec Average Function', name: 'geospatialQueryset.executionSpec.avgFunc', type: 'select', options: ['QUERY_MEDIAN', 'QUERYSET_MEAN'], default: 'QUERY_MEDIAN' },
          { label: 'Execution Spec On Cold Failure', name: 'geospatialQueryset.executionSpec.onColdFailure', type: 'select', options: ['SKIP_REMAINING_ALL_QUERY_EXECUTIONS', 'EXCLUDE_AND_PROCEED'], default: 'SKIP_REMAINING_ALL_QUERY_EXECUTIONS' },
          { label: 'Execution Spec Clear Cache Delay (ms)', name: 'geospatialQueryset.executionSpec.clearCacheDelaymSecs', type: 'number', default: 5000 }
        ],
        'scalabilityPred500M_WLoriginal.json': [
          { label: 'Class Name', name: 'classname', type: 'text', default: 'gr.uoa.di.rdf.Geographica3.runtime.workloadspecs.impl.SimpleGeospatialWL' },
          { label: 'Name', name: 'name', type: 'text', default: 'ScalabilityPred' },
          { label: 'Relative Base Dir', name: 'relativeBaseDir', type: 'text', default: '' },
          { label: 'Dataset Class Name', name: 'geospatialDataset.classname', type: 'text', default: 'gr.uoa.di.rdf.Geographica3.runtime.dataset.impl.SimpleGeospatialDS' },
          { label: 'Dataset Name', name: 'geospatialDataset.name', type: 'text', default: 'scalability_500M' },
          { label: 'Dataset Simple Geospatial Datasets (JSON)', name: 'geospatialDataset.simpleGeospatialDataSetList', type: 'textarea', default: JSON.stringify([{ name: 'scalability_500M', relativeBaseDir: '', format: 'RDF' }], null, 2), validate: 'json' },
          { label: 'Dataset Triple Count', name: 'geospatialDataset.n', type: 'number', default: 500000000 },
          { label: 'Queryset Class Name', name: 'geospatialQueryset.classname', type: 'text', default: 'gr.uoa.di.rdf.Geographica3.runtime.querysets.complex.impl.StaticTempParamQS' },
          { label: 'Queryset Name', name: 'geospatialQueryset.name', type: 'text', default: 'scalabilityPred' },
          { label: 'Queryset Has Predicate Queries', name: 'geospatialQueryset.hasPredicateQueriesAlso', type: 'select', options: ['true', 'false'], default: 'true' },
          { label: 'Queryset Map Queries (JSON)', name: 'geospatialQueryset.mapQueries', type: 'textarea', default: JSON.stringify([
            { label: 'q1', text: 'SELECT ?s ?o WHERE { ?s <http://www.opengis.net/ont/geosparql#asWKT> ?o . ?s ?p "POLYGON((23.708496093749996 37.95719224376526,22.906494140625 40.659805938378526,11.524658203125002 48.16425348854739,-0.1181030273437499 51.49506473014367,-3.2189941406250004 55.92766341247031,-5.940856933593749 54.59116279530599,-3.1668090820312504 51.47967237816337,23.708496093749996 37.95719224376526))"^^<http://www.opengis.net/ont/geosparql#wktLiteral> . }', usePredicate: true, expectedResults: 0 }
          ], null, 2), validate: 'json' },
          { label: 'Queryset Namespace Prefixes (JSON)', name: 'geospatialQueryset.mapUsefulNamespacePrefixes', type: 'textarea', default: JSON.stringify({ geosparql: 'http://www.opengis.net/ont/geosparql#' }, null, 2), validate: 'json' },
          { label: 'Queryset Template Params (JSON)', name: 'geospatialQueryset.mapTemplateParams', type: 'textarea', default: JSON.stringify({
            GIVEN_SPATIAL_LITERAL: '"POLYGON((23.708496093749996 37.95719224376526,22.906494140625 40.659805938378526,11.524658203125002 48.16425348854739,-0.1181030273437499 51.49506473014367,-3.2189941406250004 55.92766341247031,-5.940856933593749 54.59116279530599,-3.1668090820312504 51.47967237816337,23.708496093749996 37.95719224376526))"^^<http://www.opengis.net/ont/geosparql#wktLiteral>',
            FUNCTION: 'sfIntersects'
          }, null, 2), validate: 'json' },
          { label: 'Execution Spec Class Name', name: 'geospatialQueryset.executionSpec.classname', type: 'text', default: 'gr.uoa.di.rdf.Geographica3.runtime.executionspecs.impl.SimpleES' },
          { label: 'Execution Spec Repetitions (JSON)', name: 'geospatialQueryset.executionSpec.execTypeReps', type: 'textarea', default: JSON.stringify({ COLD: 3, WARM: 3 }, null, 2), validate: 'json' },
          { label: 'Execution Spec Max Duration Per Query (s)', name: 'geospatialQueryset.executionSpec.maxDurationSecsPerQueryRep', type: 'number', default: 86400 },
          { label: 'Execution Spec Max Total Duration (s)', name: 'geospatialQueryset.executionSpec.maxDurationSecs', type: 'number', default: 604800 },
          { label: 'Execution Spec Action', name: 'geospatialQueryset.executionSpec.action', type: 'select', options: ['RUN', 'TEST'], default: 'RUN' },
          { label: 'Execution Spec Average Function', name: 'geospatialQueryset.executionSpec.avgFunc', type: 'select', options: ['QUERY_MEDIAN', 'QUERYSET_MEAN'], default: 'QUERY_MEDIAN' },
          { label: 'Execution Spec On Cold Failure', name: 'geospatialQueryset.executionSpec.onColdFailure', type: 'select', options: ['SKIP_REMAINING_ALL_QUERY_EXECUTIONS', 'EXCLUDE_AND_PROCEED'], default: 'SKIP_REMAINING_ALL_QUERY_EXECUTIONS' },
          { label: 'Execution Spec Clear Cache Delay (ms)', name: 'geospatialQueryset.executionSpec.clearCacheDelaymSecs', type: 'number', default: 5000 }
        ]
      }
    }
  };

  const [formData, setFormData] = React.useState({
    category: '',
    structure: '',
    filters: {},
    errors: {}
  });
  const [jsonOutput, setJsonOutput] = React.useState('');

  React.useEffect(function() {
    const constructNestedObject = function(filters) {
      const result = {};
      Object.keys(filters).forEach(key => {
        const keys = key.split('.');
        let current = result;
        for (let i = 0; i < keys.length - 1; i++) {
          current[keys[i]] = current[keys[i]] || {};
          current = current[keys[i]];
        }
        try {
          current[keys[keys.length - 1]] = JSON.parse(filters[key]);
        } catch {
          current[keys[keys.length - 1]] = filters[key];
        }
      });
      return result;
    };
    setJsonOutput(JSON.stringify(constructNestedObject(formData.filters), null, 2));
  }, [formData.filters]);

  const validateField = function(field, value) {
    if (field.validate === 'json') {
      try {
        JSON.parse(value);
        return '';
      } catch {
        return 'Invalid JSON format';
      }
    }
    return '';
  };

  const handleCategoryChange = function(e) {
    const category = e.target.value;
    setFormData({
      category,
      structure: '',
      filters: {},
      errors: {}
    });
  };

  const handleStructureChange = function(e) {
    const structure = e.target.value;
    const defaultFilters = {};
    const defaultErrors = {};
    if (formData.category && jsonStructures[formData.category].fields[structure]) {
      jsonStructures[formData.category].fields[structure].forEach(field => {
        defaultFilters[field.name] = field.default;
        if (field.validate) {
          defaultErrors[field.name] = validateField(field, field.default);
        }
      });
    }
    setFormData(prev => ({
      ...prev,
      structure,
      filters: defaultFilters,
      errors: defaultErrors
    }));
  };

  const handleFilterChange = function(e) {
    const { name, value } = e.target;
    const field = jsonStructures[formData.category].fields[formData.structure].find(f => f.name === name);
    const error = field && field.validate ? validateField(field, value) : '';
    setFormData(prev => ({
      ...prev,
      filters: {
        ...prev.filters,
        [name]: value
      },
      errors: {
        ...prev.errors,
        [name]: error
      }
    }));
  };

  const handleSaveToFile = function() {
    const hasErrors = Object.values(formData.errors).some(error => error);
    if (hasErrors) {
      alert('Please fix validation errors before saving.');
      return;
    }
    const constructNestedObject = function(filters) {
      const result = {};
      Object.keys(filters).forEach(key => {
        const keys = key.split('.');
        let current = result;
        for (let i = 0; i < keys.length - 1; i++) {
          current[keys[i]] = current[keys[i]] || {};
          current = current[keys[i]];
        }
        try {
          current[keys[keys.length - 1]] = JSON.parse(filters[key]);
        } catch {
          current[keys[keys.length - 1]] = filters[key];
        }
      });
      return result;
    };
    const json = JSON.stringify(constructNestedObject(formData.filters), null, 2);
    const blob = new Blob([json], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = formData.structure || 'output.json';
    a.click();
    URL.revokeObjectURL(url);
  };

  const fields = formData.category && formData.structure && jsonStructures[formData.category].fields[formData.structure]
    ? jsonStructures[formData.category].fields[formData.structure]
    : [];

  return React.createElement(
    "div",
    { className: "glassmorphism p-8" },
    React.createElement("h2", { className: "text-xl font-semibold text-white mb-6" }, "Filter JSON Structures"),
    React.createElement(
      "div",
      { className: "grid grid-cols-1 md:grid-cols-2 gap-6" },
      React.createElement(
        "div",
        null,
        React.createElement("label", { className: "block text-sm font-medium text-white mb-2" }, "Category"),
        React.createElement(
          "select",
          {
            className: "input-field",
            value: formData.category,
            onChange: handleCategoryChange
          },
          React.createElement("option", { value: "" }, "Select Category"),
          categories.map(category =>
            React.createElement("option", { key: category, value: category }, category)
          )
        )
      ),
      formData.category && React.createElement(
        "div",
        null,
        React.createElement("label", { className: "block text-sm font-medium text-white mb-2" }, "Structure"),
        React.createElement(
          "select",
          {
            className: "input-field",
            value: formData.structure,
            onChange: handleStructureChange
          },
          React.createElement("option", { value: "" }, "Select Structure"),
          jsonStructures[formData.category].options.map(option =>
            React.createElement("option", { key: option, value: option }, option)
          )
        )
      )
    ),
    formData.structure && React.createElement(
      "div",
      { className: "mt-6" },
      React.createElement("h3", { className: "text-lg font-medium text-white mb-4" }, "Filters"),
      React.createElement(
        "div",
        { className: "grid grid-cols-1 md:grid-cols-2 gap-6" },
        fields.map(field => React.createElement(
          "div",
          { key: field.name },
          React.createElement("label", { className: "block text-sm font-medium text-white mb-2" }, field.label),
          field.type === 'textarea' ? React.createElement(
            "textarea",
            {
              name: field.name,
              className: "input-field textarea-field",
              value: formData.filters[field.name] || field.default,
              onChange: handleFilterChange
            }
          ) : field.type === 'select' ? React.createElement(
            "select",
            {
              name: field.name,
              className: "input-field",
              value: formData.filters[field.name] || field.default,
              onChange: handleFilterChange
            },
            field.options.map(option =>
              React.createElement("option", { key: option, value: option }, option)
            )
          ) : React.createElement(
            "input",
            {
              type: field.type,
              name: field.name,
              className: "input-field",
              value: formData.filters[field.name] || field.default,
              onChange: handleFilterChange
            }
          ),
          formData.errors[field.name] && React.createElement(
            "p",
            { className: "error-text" },
            formData.errors[field.name]
          )
        ))
      ),
      React.createElement(
        "div",
        { className: "mt-6" },
        React.createElement(
          "button",
          {
            className: "btn-primary",
            onClick: handleSaveToFile,
            disabled: Object.values(formData.errors).some(error => error)
          },
          "Save to File"
        )
      )
    ),
    formData.filters && Object.keys(formData.filters).length > 0 && React.createElement(
      "div",
      { className: "mt-8" },
      React.createElement("h3", { className: "text-lg font-medium text-white mb-4" }, "JSON Output"),
      React.createElement("pre", { className: "json-output" }, jsonOutput)
    )
  );
};

const App = function() {
  return React.createElement(
    ErrorBoundary,
    null,
    React.createElement(
      "div",
      { className: "max-w-7xl mx-auto p-6" },
      React.createElement(
        "header",
        { className: "header p-6 mb-8" },
        React.createElement("h1", { className: "text-3xl font-bold text-white" }, "Dynamic JSON Filter Dashboard")
      ),
      React.createElement(Web3Animation),
      React.createElement(FilterForm)
    )
  );
};

ReactDOM.render(React.createElement(App), document.getElementById('root'));
