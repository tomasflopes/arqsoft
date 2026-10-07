#! /bin/sh
echo "LOG: Generate Plantuml Diagrams"
exportFormat="svg"
extra="-SdefaultFontSize=20"
out="_assets"
if [ $1 ]; then
  baseDir=$1
else
  baseDir=docs
fi

for out_dir in `find $baseDir -name "$out" -type d`;
do
  echo "> Removing old output files: $out_dir"
  rm -rf $out_dir
done

echo
echo "> Generating files..."
echo

for aFile in `find $baseDir -name "*.puml" -type f`;
do
  echo "> Processing file: $aFile"
  file=$(echo $aFile | cut -d "/" -f 3)
  echo " >> Output file: ./$file.$exportFormat"

	java -jar libs/plantuml-1.2023.1.jar $extra -t$exportFormat $aFile -o$out
done

echo "Finished"
