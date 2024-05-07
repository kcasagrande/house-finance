import * as d3 from 'd3';
import React from 'react';
import { Stack, Tooltip } from '@mui/material';
import { amount, percent } from '../format';
import './PieChart.css';

function PieChart({colors, data, padAngle = 0.01, width, height, name = (datum) => datum.name, value = (datum) => datum.value}) {
  const sum = data.map(value).reduce((sum, element) => sum + element, 0);

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
    .innerRadius(40)
    .outerRadius(100)
    .cornerRadius(0);
  
  const labelRadius = arcGenerator.outerRadius()() * 0.8;
  const labelArcGenerator = d3.arc()
    .innerRadius(labelRadius)
    .outerRadius(labelRadius);
  
  const arcs = pieGenerator(data);

  function SvgLabel({arc}) {
    return (
      <React.Fragment key={name(arc.data)}>
        <text fontSize="6" fontWeight="bold" transform={'translate(' + labelArcGenerator.centroid(arc) + ')'}>
          <tspan y="-0.4em">{name(arc.data)}</tspan>
        </text>
        <text fontSize="4" transform={'translate(' + labelArcGenerator.centroid(arc) + ')'}>
          <tspan y="0.7em">{amount(value(arc.data)) + ' - ' + percent()(value(arc.data) / sum)}</tspan>
        </text>
      </React.Fragment>
    );
  }

  function HtmlLabel({arc}) {
    return (
      <Stack key={name(arc.data)} direction="column" alignItems="center">
        <div>{name(arc.data)}</div>
        <div>{amount(value(arc.data)) + ' - ' + percent()(value(arc.data) / sum)}</div>
      </Stack>
    );
  }

  return (
    <svg width={width} height={height} viewBox="-110 -110 220 220" style={{maxWidth: '100%', height: 'auto'}}>
      <g>
        {arcs.map((arc) =>
          <Tooltip key={name(arc.data)} title={<HtmlLabel arc={arc} />}>
            <path className="pie-arc" fill={color(name(arc.data))} d={arcGenerator(arc)} />
          </Tooltip>
        )}
      </g>
      <g textAnchor="middle">
        {arcs
          .filter((arc) => arc.endAngle - arc.startAngle > 0.1)
          .map((arc) => <SvgLabel key={name(arc.data)} arc={arc} />)
        }
      </g>
    </svg>
  );
}

export default PieChart;