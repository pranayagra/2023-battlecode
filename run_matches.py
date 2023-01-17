from itertools import product
import subprocess
import time

emojiMode = True
emojiMap = {
    'Won': ':heavy_check_mark:',
    'Lost': ':x:',
    'Tied': ':grimacing:',
    'N/A': ':heavy_minus_sign:',
    'Error': ':heavy_exclamation_mark:'
}
errors = []
currentBot = 'basicbot'

bots = ['spawnorder']
botsSet = set(bots)
# maps = ['maptestsmall', 'SmallElements', 'DefaultMap', 'AllElements', 'TestFarWell', 'TestFarWell2', 'zzBuggyForest', 'zzConcentricEvil', 'zzCornerTrouble', 'zzDuels', 'zzHighwayToHell', 'zzItsATrap', 'zzMinimalism', 'zzOverload', 'zzRingAroundTheRosie', 'zzzHyperRush']
# maps = ['maptestsmall', 'SmallElements', 'DefaultMap', 'AllElements']
maps = ['SmallElements']
mapsSet = set(maps)

matches = set(product(bots, maps))

numWinsMapping = {
    0: 'Lost',
    1: 'Tied',
    2: 'Won',
}


def retrieveTotalUnitsSpawned(output):
    totalValueA = 0
    for i in range(4):
        startString = f'HQA{i} ('
        startIndex = output.find(startString)
        if startIndex == -1:
            continue
        endIndex = output.find(')', startIndex)
        if endIndex == -1:
            continue
        print(startIndex, endIndex)
        value = output[startIndex + len(startString):endIndex]
        print(value)
        totalValueA += int(value)
    print(totalValueA)

    totalValueB = 0
    for i in range(4):
        startString = f'HQB{i}--'
        startIndex = output.find(startString)

        if startIndex == -1:
            continue
        endIndex = output.find('--', startIndex)
        if endIndex == -1:
            continue
        value = output[startIndex + len(startString) + 1:endIndex]
        print(value)
        totalValueB += int(value)
    print(totalValueB)

    return (totalValueA, totalValueB)



def retrieveGameLength(output):
    startIndex = output.find('wins (round ')
    if startIndex == -1:
        return -1
    endIndex = output.find(')', startIndex)
    if endIndex == -1:
        return -1
    return output[startIndex + len('wins(round ') + 1:endIndex]

def run_match(bot, map):
    print("Running {} vs {} on {}".format(currentBot, bot, map))
    start_time = time.time()
    try:
        outputA = str(subprocess.check_output(['./gradlew', 'run', '-PteamA=' + currentBot, '-PteamB=' + bot, '-Pmaps=' + map]))
        print('after: ', time.time() - start_time)
        outputB = str(subprocess.check_output(['./gradlew', 'run', '-PteamA=' + bot, '-PteamB=' + currentBot, '-Pmaps=' + map]))
        print('after: ', time.time() - start_time)
    except subprocess.CalledProcessError as exc:
        print("Status: FAIL", exc.returncode, exc.output)
        return 'Error'
    else:
        winAString = '{} (A) wins'.format(currentBot)
        winBString = '{} (B) wins'.format(currentBot)
        loseAString = '{} (B) wins'.format(bot)
        loseBString = '{} (A) wins'.format(bot)
        # print("outputaA type: {}, {}".format(type(outputA), outputA))
        
        numWins = 0

        print('outputA: ', outputA)
        print('outputB: ', outputB)
        
        gameLengthA = retrieveGameLength(outputA)
        gameLengthB = retrieveGameLength(outputB)

        AMoreUnits = 0
        BMoreUnits = 0
        if gameLengthA == 2000 or True:
            totalUnitSpawnedT1A, totalUnitSpawnedT2A = retrieveTotalUnitsSpawned(outputA)
            AMoreUnits = totalUnitSpawnedT1A - totalUnitSpawnedT2A
            print('totalUnitSpawnedT1A: ', totalUnitSpawnedT1A, 'totalUnitSpawnedT2A: ', totalUnitSpawnedT2A, 'AMoreUnits: ', AMoreUnits)
        if gameLengthB == 2000 or True:
            totalUnitSpawnedT1B, totalUnitSpawnedT2B = retrieveTotalUnitsSpawned(outputB)
            BMoreUnits = totalUnitSpawnedT2B - totalUnitSpawnedT1B
            print('totalUnitSpawnedT1B: ', totalUnitSpawnedT1B, 'totalUnitSpawnedT2B: ', totalUnitSpawnedT2B, 'BMoreUnits: ', BMoreUnits)

        if winAString in outputA:
            numWins += 1
        else:
            if not loseAString in outputA:
                return 'Error'
        if winBString in outputB:
            numWins += 1
        else:
            if not loseBString in outputB:
                return 'Error'
        return numWinsMapping[numWins] + ' (' + ', '.join([gameLengthA, gameLengthB]) + ')' + ' (' + ', '.join([str(AMoreUnits), str(BMoreUnits)]) + ')'


results = {}
# Run matches
for bot, map in matches:
    # Verify match is valid
    if not bot in botsSet or not map in mapsSet:
        errors.append('Unable to parse bot={}, map={}'.format(bot, map))
    # run run_match.py
    
    results[(bot, map)] = run_match(bot, map)

# Construct table
table = [[results.get((bot, map), 'N/A') for bot in bots] for map in maps]

def replaceWithDictionary(s, mapping):
    for a, b in mapping.items():
        s = s.replace(a, b)
    return s

if emojiMode:
    table = [[replaceWithDictionary(item, emojiMap) for item in row] for row in table]

# Write to file
with open('matches-summary.txt', 'w') as f:
    table = [[''] + bots, [':---:' for i in range(len(bots) + 1)]] + [[map] + row for map, row in zip(maps, table)]
    for line in table:
        f.write('| ')
        f.write(' | '.join(line))
        f.write(' |')
        f.write('\n')
    f.write('\n')
    for error in errors:
        f.write(error)
