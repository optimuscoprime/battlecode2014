default: commit

commit:
	ant build
	git add .
	git commit -a
	git push

submission:
	ant clean
	ant build
	ant -Dteam=team085 jar
