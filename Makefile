default: submission

submission:
	ant clean
	ant build
	ant -Dteam=team085 jar
