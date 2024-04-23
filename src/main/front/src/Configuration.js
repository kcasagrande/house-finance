import RestaurantIcon from '@mui/icons-material/Restaurant';
import RoofingIcon from '@mui/icons-material/Roofing';
import WaterIcon from '@mui/icons-material/Water';

const Configuration = {
  api: 'http://localhost:8080/api/v1',
  locale: 'fr',
  categoryIcons: {
    'Alimentaire': <RestaurantIcon />,
    'Assurance maison': <RoofingIcon />,
    'Eau': <WaterIcon />
  }
};

export default Configuration;