$Env:CLASSPATH = ".;./lib/postgresql.jar;./lib/postgis.jar"
echo "Choix de la question : "
echo "1 - Question11"
echo "2 - Question12"
echo "3 - Question13"
echo "4 - Question14b"
echo "5 - Question14c"
echo "0 - Exit"
echo "----------------"
$choix = Read-Host -Prompt 'Choix : '

if ($choix -eq "1")
{
	java tpsig.Question11
}elseif ($choix -eq "2")
{
	java tpsig.Question12
}elseif ($choix -eq "3")
{
	java tpsig.Question13
}elseif ($choix -eq "4")
{
	java tpsig.Question14b
}elseif ($choix -eq "5")
{
	java tpsig.Question14c
}
else
{
	exit
}
