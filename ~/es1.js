const arr = ["mela", "pera", "mandarino", "pesca", "banana"] 
const output = [["mela", "mandarino"], ["pesca", "pera"], ["banana"]]

const map = {}

for (var i = 0; i < arr.length(); i++){
    if (map[arr[i].charAt(0)] == null)
        map[arr[i].charAt(0)] = [arr[i]]
    else
        map[arr[i]].push(arr[i])
}