package pt.psoft.g1.psoftg1.external.schemas;

import java.util.List;

public class OpenLibraryResponse {
  public List<Doc> docs;

  public static class Doc {
    public List<String> isbn;
    public List<String> ia;
  }
}
