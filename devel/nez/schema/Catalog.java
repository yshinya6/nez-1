package nez.schema;

public class Catalog {
	@Schematic
	public Book[] list;

	public class Book {
		@Schematic
		@Length(max = 10, min = 0)
		public String id;
		@Schematic
		@Enumeration({ "Jane", "John" })
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
