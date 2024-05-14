import {
  Restaurant as AlimentaireIcon,
  Roofing as AssuranceMaisonIcon,
  Water as EauIcon,
  Bolt as ElectriciteIcon,
  LocalGasStation as EssenceIcon,
  AccountBalance as FraisBancairesIcon,
  Propane as FuelIcon,
  PropaneTank as GazIcon,
  LocalAtm as ImpotsIcon,
  Router as InternetIcon,
  Casino as LoisirIcon,
  House as MaisonIcon,
  AccountCircle as PersonnelIcon,
  Toll as PeageIcon,
  Key as PretImmobilierIcon,
  LocalBar as RestaurantIcon,
  LocalHospital as SanteIcon,
  LocalParking as StationnementIcon,
  NightShelter as TaxeDHabitationIcon,
  DirectionsCar as VoitureIcon
} from '@mui/icons-material';
import RoofingIcon from '@mui/icons-material/Roofing';
import WaterIcon from '@mui/icons-material/Water';

const Configuration = {
  api: 'http://localhost:8080/api/v1',
  locale: 'fr-FR',
  currency: 'EUR',
  categoryIcons: {
    'Alimentaire': <AlimentaireIcon fontSize="small" />,
    'Assurance maison': <AssuranceMaisonIcon fontSize="small" />,
    'Eau': <EauIcon fontSize="small" />,
    'Essence': <EssenceIcon fontSize="small" />,
    'Frais bancaires': <FraisBancairesIcon fontSize="small" />,
    'Fuel': <FuelIcon fontSize="small" />,
    'Gaz': <GazIcon fontSize="small" />,
    'Impôts': <ImpotsIcon fontSize="small" />,
    'Internet': <InternetIcon fontSize="small" />,
    'Loisir': <LoisirIcon fontSize="small" />,
    'Maison': <MaisonIcon fontSize="small" />,
    'Personnel': <PersonnelIcon fontSize="small" />,
    'Péage': <PeageIcon fontSize="small" />,
    'Prêt immobilier': <PretImmobilierIcon fontSize="small" />,
    'Restaurant': <RestaurantIcon fontSize="small" />,
    'Santé': <SanteIcon fontSize="small" />,
    'Stationnement': <StationnementIcon fontSize="small" />,
    'Taxe d\'habitation': <TaxeDHabitationIcon fontSize="small" />,
    'Voiture': <VoitureIcon fontSize="small" />,
    'Électricité': <ElectriciteIcon fontSize="small" />
  }
};

export default Configuration;