package pt.psoft.g1.psoftg1.external.schemas;

public class GoogleBooksResponse {
  public Item[] items;

  public static class Item {
    public VolumeInfo volumeInfo;
  }

  public static class VolumeInfo {
    public String title;
    public String[] authors;
    public String publishedDate;
    public String description;
    public IndustryIdentifier[] industryIdentifiers;
  }

  public static class IndustryIdentifier {
    public String type;
    public String identifier;
  }
}
