import { useState, useRef } from 'react';
import { Button, TextField } from '@mui/material';

function FileChooser({onChange}) {
  const [selectedFile, setSelectedFile] = useState(null);
  const fileInput = useRef();
  
  function handleChange(event) {
    setSelectedFile(event.target.files[0]);
    onChange(event.target.files[0]);
  };
  
  return (
    <>
      <input ref={fileInput} type="file" accept="text/csv,.csv" onChange={handleChange} style={{display: 'none'}} />
      <Button variant="outlined" onClick={() => fileInput.current.click()}>Select file</Button>
      <TextField variant="standard" InputProps={{readOnly: true}} value={(selectedFile && selectedFile.name) || ''}></TextField>
    </>
  );
}

export default FileChooser;