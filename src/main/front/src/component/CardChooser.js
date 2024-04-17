import { FormControl, InputLabel, MenuItem, Select } from '@mui/material';

function CardChooser({id, operation, cards = [], onChange, sx }) {
  return (
    <FormControl sx={{ ...sx, width: 100 }}>
      <InputLabel id={id + "-label"}>Card</InputLabel>
      <Select
        id={id}
        labelId={id + "-label"}
        label="Card"
        onChange={event => onChange(event.target.value)}
        defaultValue={operation.card || ''}
      >
        <MenuItem key="" value=""></MenuItem>
        {cards.map((card) =>
          <MenuItem key={card.number} value={card.number}>{card.number.substring(12, 16)} - {card.holder.name}</MenuItem>
        )}
      </Select>
    </FormControl>
  );
}

export default CardChooser;