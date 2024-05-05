import * as d3 from 'd3';

function PieChart({colors, data, padAngle = 0.01, width, height, name = (datum) => datum.name, value = (datum) => datum.value}) {

  const color = d3.scaleOrdinal()
    .domain(data.map(name).toSorted())
    .range(d3.quantize(d3.interpolateRgbBasis(colors), data.length));
    
  const pieGenerator = d3.pie()
    .startAngle(0)
    .endAngle(2 * Math.PI)
    .padAngle(padAngle)
    .value(value)
    .sort((a, b) => d3.ascending(name(a), name(b)));
    
  const arcGenerator = d3.arc()
    .innerRadius(0)
    .outerRadius(100)
    .cornerRadius(0);
  
  const arcs = pieGenerator(data);

  return (
    <svg width={width} height={height} viewBox="-100 -100 200 200" style={{maxWidth: '100%', height: 'auto'}}>
      <g>
        {arcs.map(arc => <path key={name(arc.data)} fill={color(name(arc.data))} d={arcGenerator(arc)} />)}
      </g>
    </svg>
  );
}

export default PieChart;