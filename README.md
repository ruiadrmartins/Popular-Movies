# Popular-Movies
This Project uses The Movie Database (TMDB) to fetch information on the Movies.

To run this project you will need to obtain an API key, or it will not work.
If you do not have an API key, you first need to <a href="https://www.themoviedb.org/account/signup">register</a> an account on the website.

In your request for a key, state that your usage will be for educational/non-commercial use. You will also need to provide some personal information to complete the request. Once you submit your request, you should receive your key via email shortly after.

After you get the API key, access the <a href="gradle.properties">gradle.properties</a> file and
replace line 19:
MyTMDBApiKey="NEWFAKEDATA"
with 
MyTMDBApiKey="[YOURAPIKEY]"

where [YOURAPIKEY] is your API key.
