package deeplearning;
import java.util.*;
import java.time.LocalDate;

enum Colorsss {
    BLACK, WHITE, BLUE, RED, GREEN, BROWN, YELLOW, GRAY, PURPLE,
    ORANGE, PINK, MAROON, BEIGE, GOLD, SILVER
}

enum PatternType {
    PLAIN, STRIPED, CHECKERED, FLORAL, PRINTED, EMBROIDERED
}

enum FabricType {
    COTTON, DENIM, WOOL, SILK, LINEN, LEATHER, POLYESTER, CHIFFON, VELVET, KNIT
}

enum Season {
    SPRING, SUMMER, FALL, WINTER
}

enum FitType {
    SLIM, REGULAR, LOOSE, SKINNY, TAILORED
}

enum PantsType {
    JEANS, CHINOS, DRESS_PANTS, JOGGERS, SHORTS, CARGO, SWEATPANTS, SHALWAR
}

enum ShirtType {
    TSHIRT, DRESS_SHIRT, POLO, HENLEY, CASUAL_SHIRT, KURTA, KAMEEZ,
    LONG_SLEEVE, SHORT_SLEEVE, TUNIC
}

enum ShoeType {
    SNEAKERS, FORMAL, LOAFERS, BOOTS, SANDALS, SLIPPERS, KHUSSA,
    PESHAWERI, CHAPPAL, MOJARI, OXFORDS, BROGUES
}

enum CulturalType {
    SHALWAR, KAMEEZ, KURTA, WAISTCOAT, SHERWANI, ACHKAN, DUPATTA,
    CHADAR, LEHNGA, SAARI
}

abstract class clothing {
    private String id;
    private Colorsss primaryColor;
    private List<Colorsss> accentColors;
    private String size;
    private PatternType pattern;
    private FabricType fabric;
    private EnumSet<Season> seasons;
    private FitType fit;
    private String notes;
    private List<String> tags;
    private LocalDate lastWornDate;

    public clothing(String id, Colorsss primaryColor, List<Colorsss> accentColors, String size,
                        PatternType pattern, FabricType fabric, EnumSet<Season> seasons,
                        FitType fit, String notes, List<String> tags, LocalDate lastWornDate) {
        this.id = id;
        this.primaryColor = primaryColor;
        this.accentColors = accentColors;
        this.size = size;
        this.pattern = pattern;
        this.fabric = fabric;
        this.seasons = seasons;
        this.fit = fit;
        this.notes = notes;
        this.tags = tags;
        this.lastWornDate = lastWornDate;
    }

    public String getId() { return id; }
    public Colorsss getPrimaryColor() { return primaryColor; }
    public List<Colorsss> getAccentColors() { return accentColors; }
    public String getSize() { return size; }
    public PatternType getPattern() { return pattern; }
    public FabricType getFabric() { return fabric; }
    public EnumSet<Season> getSeasons() { return seasons; }
    public FitType getFit() { return fit; }
    public String getNotes() { return notes; }
    public List<String> getTags() { return tags; }

    public String toString() {
        return "ID: " + id +
               "\nPrimary Color: " + primaryColor +
               "\nAccent Colors: " + accentColors +
               "\nSize: " + size +
               "\nPattern: " + pattern +
               "\nFabric: " + fabric +
               "\nSeasons: " + seasons +
               "\nFit: " + fit +
               "\nNotes: " + notes +
               "\nTags: " + tags+
               "\nLast Worn: "+ lastWornDate;
    }
    
public LocalDate getLastWornDate() {
    return lastWornDate;
}

public void setLastWornDate(LocalDate date) {
    this.lastWornDate = date;
}
}

class Pants extends clothing {
    private PantsType pantsType;

    public Pants(String id, Colorsss primaryColor, List<Colorsss> accentColors, String size,
                 PatternType pattern, FabricType fabric, EnumSet<Season> seasons,
                 FitType fit, String notes, List<String> tags,  LocalDate lastWornDate, PantsType pantsType) {
        super(id, primaryColor, accentColors, size, pattern, fabric, seasons, fit, notes, tags, lastWornDate);
        this.pantsType = pantsType;
    }

    public PantsType getPantsType() {
        return pantsType;
    }

    @Override
    public String toString() {
        return "" + super.toString() + "\nType: " + pantsType  + "\nEnd" + "\n\n";
    }
}

class Shirt extends clothing {
    private ShirtType shirtType;

    public Shirt(String id, Colorsss primaryColor, List<Colorsss> accentColors, String size,
                 PatternType pattern, FabricType fabric, EnumSet<Season> seasons,
                 FitType fit, String notes, List<String> tags,  LocalDate lastWornDate, ShirtType shirtType) {
        super(id, primaryColor, accentColors, size, pattern, fabric, seasons, fit, notes, tags, lastWornDate);
        this.shirtType = shirtType;
    }

    public ShirtType getShirtType() {
        return shirtType;
    }

    @Override
    public String toString() {
        return "" + super.toString() + "\nType: " + shirtType + "\nEnd" + "\n\n";
    }
}

class Shoe extends clothing {
    private ShoeType shoeType;

    public Shoe(String id, Colorsss primaryColor, List<Colorsss> accentColors, String size,
                PatternType pattern, FabricType fabric, EnumSet<Season> seasons,
                FitType fit, String notes, List<String> tags, LocalDate lastWornDate, ShoeType shoeType) {
        super(id, primaryColor, accentColors, size, pattern, fabric, seasons, fit, notes, tags, lastWornDate);
        this.shoeType = shoeType;
    }

    public ShoeType getShoeType() {
        return shoeType;
    }

    @Override
    public String toString() {
        return "" + super.toString() + "\nType: " + shoeType + "\nEnd" + "\n\n";
    }
}

class CulturalDress extends clothing {
    private CulturalType culturalType;

    public CulturalDress(String id, Colorsss primaryColor, List<Colorsss> accentColors, String size,
                         PatternType pattern, FabricType fabric, EnumSet<Season> seasons,
                         FitType fit, String notes, List<String> tags, LocalDate lastWornDate, CulturalType culturalType) {
        super(id, primaryColor, accentColors, size, pattern, fabric, seasons, fit, notes, tags, lastWornDate);
        this.culturalType = culturalType;
    }

    public CulturalType getCulturalType() {
        return culturalType;
    }

    @Override
    public String toString() {
        return "" + super.toString() + "\nType: " + culturalType + "\nEnd" + "\n\n";
    }
}
