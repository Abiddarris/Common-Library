cd common

find src/main/java -path src/main/java/com/abiddarris/common/android -prune -o -type f -name "*.java" -print > build/classes.txt

if ! [ -a build/classes ]; then
    mkdir build/classes
fi
echo "Main.java" >> build/classes.txt

javac -d build/classes @build/classes.txt

if [ $? -ne 0 ]; then
    exit 1
fi

java -cp build/classes Main