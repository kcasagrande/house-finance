import LocalGasStationIcon from '@mui/icons-material/LocalGasStation';
import RestaurantIcon from '@mui/icons-material/Restaurant';
import RoofingIcon from '@mui/icons-material/Roofing';
import WaterIcon from '@mui/icons-material/Water';

const Configuration = {
  api: 'http://localhost:8080/api/v1',
  locale: 'fr-FR',
  currency: 'EUR',
  categoryIcons: {
    'Alimentaire': <RestaurantIcon fontSize="small" />,
    'Assurance maison': <RoofingIcon fontSize="small" />,
    'Eau': <WaterIcon fontSize="small" />,
    'Essence': <LocalGasStationIcon fontSize="small" />
  }
};

export default Configuration;