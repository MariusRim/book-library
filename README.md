# Book library 

This is a simple book library project made using springboot. Since the requirements for this application specified 
only backend functionality, no front end implementation is present. Another requirement for this project was not to use 
a database and instead persist data via local files.

## How to run
1. Maven -> book-library -> Lifecycle -> package
2. Go to ../book-library/target
3. Copy and paste the book-storage-files folder from the project path here
4. If there is no book-storage-files folder, create a new one in the target directory
    1. Create all-books.json and book-reservations.json files
    1. add [] to each file, otherwise the reading algorithm will throw an error
5. Run book-library-FINAL.jar

! Important !  
book-storage-files directory, all-books.json and all-reservations.json must all be present in the target 
directory if launching the application from the .jar file. Otherwise, a service exception will be thrown with all requests because of how the data persistence works in 
this project

## Rest calls
* Get "/v1/books" - get a list of books. You can filter the request by adding request parameters.   
  * Supported parameters: name, author, category, language, isbn, onlyTaken, onlyAvailable
* Get "/v1/books/{bookId}" - get a specific book by its guid
* Delete "/v1/books/{bookId}" - delete a specific book by its guid
* Post "/v1/books" - post a new book. Must include a Book object in the request body
* Post "/v1/books/reserve/{bookId}" - take a specified book. Must include a BookReservation object in the request body. 
  BookReservation bookGuid field will be overwritten by the bookId request parameter