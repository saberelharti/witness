package net.dm73.plainpress.model;



public class FaceCharacter {

    private String ressourceName;
    private boolean selected;

    public FaceCharacter(String ressourceName, boolean selected) {
        this.ressourceName = ressourceName;
        this.selected = selected;
    }

    public String getRessourceName() {
        return ressourceName;
    }

    public void setRessourceName(String ressourceName) {
        this.ressourceName = ressourceName;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
