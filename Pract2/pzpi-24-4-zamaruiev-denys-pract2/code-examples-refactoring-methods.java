// ==========================================
// 1. Метод: Replace Error Code with Exception
// ==========================================

// ДО РЕФАКТОРИНГУ (Поганий код)
public CarTrim getCarTrimBad(Long trimId) {
    CarTrim trim = carTrimRepository.findById(trimId).orElse(null);
    
    if (trim == null) {
        return null; 
    }
    return trim;
}

// ПІСЛЯ РЕФАКТОРИНГУ (Гарний код)
public CarTrim getCarTrimGood(Long trimId) {
    return carTrimRepository.findById(trimId)
            .orElseThrow(() -> new IllegalArgumentException("Комплектацію не знайдено: " + trimId));
}

// ==========================================
// 2. Метод: Separate Query from Modifier
// ==========================================

// ДО РЕФАКТОРИНГУ (Поганий код - змішування запису і читання)
public ConfigResponse saveConfigurationBad(ConfigRequest request) {
    // ... розрахунки ...
    SavedConfig saved = savedConfigRepository.save(savedConfig);
    
    return getByShareCode(saved.getShareCode()); 
}

// ПІСЛЯ РЕФАКТОРИНГУ (Гарний код - ізоляція)
public String saveConfigurationGood(ConfigRequest request) {
    // ... розрахунки ...
    SavedConfig saved = savedConfigRepository.save(savedConfig);
    return saved.getShareCode(); 
}

public ConfigResponse getByShareCodeGood(String shareCode) {
    SavedConfig config = savedConfigRepository.findByShareCode(shareCode)
            .orElseThrow(() -> new IllegalArgumentException("Не знайдено: " + shareCode));
    // ...
    return mapToSavedConfigResponse(config, basePrice);
}

// ==========================================
// 3. Метод: Replace Method with Method Object
// ==========================================

// ДО РЕФАКТОРИНГУ (Поганий код - громіздкий метод)
public PriceResponse calculatePriceBad(PriceRequest request) {
    CarTrim trim = carTrimRepository.findById(request.getTrimId()).orElseThrow();
    
    BigDecimal basePrice = trim.getCarModel().getBasePrice().add(trim.getPriceModifier());
    List<Option> options = request.getSelectedOptionIds() != null 
            ? optionRepository.findAllById(request.getSelectedOptionIds()) 
            : Collections.emptyList();
    
    BigDecimal optionsCost = options.stream()
            .map(Option::getPriceModifier)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
            
    BigDecimal discount = BigDecimal.ZERO;
    BigDecimal totalPrice = basePrice.add(optionsCost).subtract(discount);

    return new PriceResponse(basePrice, optionsCost, discount, totalPrice);
}

// ПІСЛЯ РЕФАКТОРИНГУ (Гарний код - винесення в об'єкт)
public PriceResponse calculatePriceGood(PriceRequest request) {
    return new PriceCalculator(carTrimRepository, optionRepository, request).compute();
}

public class PriceCalculator {
    private final CarTrimRepository carTrimRepository;
    private final OptionRepository optionRepository;
    private final PriceRequest request;
    
    private BigDecimal basePrice;
    private BigDecimal optionsCost;
    private BigDecimal discount = BigDecimal.ZERO;

    public PriceCalculator(CarTrimRepository carTrimRepository, OptionRepository optionRepository, PriceRequest request) {
        this.carTrimRepository = carTrimRepository;
        this.optionRepository = optionRepository;
        this.request = request;
    }

    public PriceResponse compute() {
        calculateBasePrice();
        calculateOptionsCost();
        
        BigDecimal totalPrice = basePrice.add(optionsCost).subtract(discount);
        return new PriceResponse(basePrice, optionsCost, discount, totalPrice);
    }
    
    private void calculateBasePrice() {
        CarTrim trim = carTrimRepository.findById(request.getTrimId()).orElseThrow();
        basePrice = trim.getCarModel().getBasePrice().add(trim.getPriceModifier());
    }
    
    private void calculateOptionsCost() {
        List<Option> options = request.getSelectedOptionIds() != null 
                ? optionRepository.findAllById(request.getSelectedOptionIds()) 
                : Collections.emptyList();
        optionsCost = options.stream()
                .map(Option::getPriceModifier)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
