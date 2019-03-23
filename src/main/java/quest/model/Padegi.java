package quest.model;

public enum Padegi {
    IMEN("им"),
    ROD("род"),
    DAT("дат"),
    VIN("вин"),
    TVOR("твор"),
    PRED("пр");

    private String type;

    public String getType() {
        return type;
    }

    Padegi(String type) {
        this.type = type;
    }


}
