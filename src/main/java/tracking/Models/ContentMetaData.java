package tracking.Models;

import java.util.List;

public class ContentMetaData {
    private List<String> categories;
    private String source;
    private String type;
    private List<String> tagsPlot;
    private List<String> listStructureId;

    public ContentMetaData(List<String> categories, String source, String type) {
        super();
        this.categories = categories;
        this.source = source;
        this.type = type;
    }

    public ContentMetaData(List<String> categories, String source, String type, List<String> tagsPlot,
                           List<String> listStructureId) {
        super();
        this.categories = categories;
        this.source = source;
        this.type = type;
        this.tagsPlot = tagsPlot;
        this.listStructureId = listStructureId;
    }

    public List<String> getCategories() {
        return categories;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

    public List<String> getTagsPlot() {
        return tagsPlot;
    }

    public void setTagsPlot(List<String> tagsPlot) {
        this.tagsPlot = tagsPlot;
    }

    public List<String> getListStructureId() {
        return listStructureId;
    }

    public void setListStructureId(List<String> listStructureId) {
        this.listStructureId = listStructureId;
    }

    public String getSource() {
        return source;
    }

    public String getType() {
        return type;
    }
}
