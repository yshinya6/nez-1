package nez.schema;

public class Catalog {
	@Schematic
	public Book[] list;

	public class Book {
		@Schematic
		public String id;
		@Schematic
		public String author;
		@Schematic
		public String title;
	}
}

/**
 * <Book id="hoge"> <author>Kimio Kuramitsu</author> <title>Konoha</title>
 * <price>100</price> <desc> This is a really <b> grate </b> book in the
 * world.</desc> </Book>
 **/
