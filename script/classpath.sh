CP=""
for top in "src" \
    "lib/clojurescript/src" \
    "lib/one/src/lib" \
    "lib/domina/src"; do
    for dir in "clj" "cljs"; do
        CP="$CP:$top/$dir"
    done
done

for JAR in lib/*.jar; do
    CP="$CP:$JAR"
done
