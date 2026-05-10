package Assigment_3_OOP;

import java.util.ArrayList;
import java.util.List;

public class LevelLayouts {

    public static double[][] getEnemySpawns(int level, int sw, int sh) {
        double sx = sw, sy = sh;
        switch (level) {
            case 1:  return new double[][]{
                {sx / 4, 30, 0, sx / 2},                                   // Ground Left (Enemy 1)
                {sx / 2 + sx / 4, 30, sx / 2, sx},                         // Ground Right (Enemy 4)
                {sx - 1150 + 375, 120, sx - 1150, sx - 1150 + 750},        // Floor 1 Mid (Enemy 1)
                {280 + 175, 400, 280, 280 + 350},                          // Floor 3 Left (Enemy 4)
                {sx - 600 + 175, 400, sx - 600, sx - 600 + 350}            // Floor 3 Right (Enemy 4)
            };
            case 2:  return new double[][]{
                {sx / 4, 30, 0, sx / 2},                                   // Ground Left (Enemy 4)
                {sx / 2 + sx / 4, 30, sx / 2, sx},                         // Ground Right (Enemy 4)
                {sx - 1000 + 225, 100, sx - 1000, sx - 1000 + 450},        // F1 Mid (Enemy 1)
                {330 + 190, 250, 330, 330 + 380},                          // F2 Left (Enemy 3)
                {sx - 700 + 165, 250, sx - 700, sx - 700 + 330},           // F2 Right (Enemy 3)
                {sx - 1000 + 225, 400, sx - 1000, sx - 1000 + 450}         // F3 Top (Enemy 1)
            };
            case 3:  return new double[][]{
                {sx / 4, 30, 0, sx / 2},                                   // Ground Left (Enemy 4)
                {sx / 2 + sx / 4, 30, sx / 2, sx},                         // Ground Right (Enemy 4)
                {180 + 175, 140 + 30, 180, 180 + 350},                     // Floor 1 Left (Enemy 1)
                {sx - 530 + 175, 140 + 30, sx - 530, sx - 530 + 350},      // Floor 1 Right (Enemy 3)
                {sx - 1020 + 250, 270 + 30, sx - 1020, sx - 1020 + 500},   // Floor 2 Mid (Enemy 2)
                {320 + 115, 500 + 30, 320, 320 + 230},                     // Floor 4 Left (Enemy 3)
                {sx - 520 + 115, 500 + 30, sx - 520, sx - 520 + 230}       // Floor 4 Right (Enemy 1)
            };
            case 4:  return new double[][]{
                {sx / 6, 30, 0, sx / 3},                                   // Ground Left (Enemy 1)
                {sx / 2, 30, sx / 3, 2 * sx / 3},                          // Ground Mid (Enemy 4)
                {sx - sx / 6, 30, 2 * sx / 3, sx},                         // Ground Right (Enemy 1)
                {sx / 2, 200, sx / 2 - 350, sx / 2 + 350},                 // Floor 2 Mid (Enemy 2)
                {210, 330, 0, 420},                                        // Floor 3 Left (Enemy 3) - stop at pillar
                {sx - 210, 330, sx - 420, sx},                             // Floor 3 Right (Enemy 3) - stop at pillar
                {sx / 2, 550, sx / 2 - 250, sx / 2 + 250}                  // Floor 4 Top (Enemy 2)
            };
            case 5:  return new double[][]{
                {sx / 2 - 100, 30, 350, sx / 2},                           // Ground Left (Enemy 1) - bw blocks
                {sx / 2 + 100, 30, sx / 2, sx - 350},                     // Ground Right (Enemy 1) - bw blocks
                {sx / 2, 230, sx / 2 - 450, sx / 2 + 420},                 // Floor 2 Mid (Enemy 2) - very long
                {sx / 2, 370, sx / 2 - 250, sx / 2 + 230},                 // Floor 3 Mid (Enemy 2)
                {195, 470, 0, 390},                                        // Floor 4 Left (Enemy 5) - stop at nub
                {sx - 195, 470, sx - 390, sx},                             // Floor 4 Right (Enemy 5) - stop at nub

            };
            case 6:  return new double[][]{
                {180 + 230, 30, 0, 640},                                   // Ground Left (Enemy 5) - split between blocks
                {sx - 180 - 230, 30, sx - 640, sx},                         // Ground Right (Enemy 5) - split between blocks
                {142, 260, 0, 285},                                        // Floor 2 Left Outer (Enemy 2)
                {sx - 142, 260, sx - 285, sx},                             // Floor 2 Right Outer (Enemy 2)
                {580 + 180, 395, 580, 580 + 360},                          // Floor 4 Mid (Enemy 5)
                {245, 480, 0, 490},                                        // Floor 5 Left Main (Enemy 1)
                {sx - 245, 480, sx - 490, sx}                              // Floor 5 Right Main (Enemy 1)
            };
            case 7:  return new double[][]{
                {sx / 4, 30, 0, sx / 2},                                   // Ground Left (Enemy 2)
                {sx / 2 + sx / 4, 30, sx / 2, sx},                         // Ground Right (Enemy 2)
                {445 / 2, 260, 0, 445},                                    // Floor 2 Left (Enemy 3) - stop at nub
                {sx - 445 / 2, 260, sx - 445, sx},                         // Floor 2 Right (Enemy 3) - stop at nub
                {sx / 2, 410, sx / 2 - 200, sx / 2 + 200},                 // Floor 3 Center (Enemy 5)
                {445 / 2, 550, 0, 445},                                    // Floor 4 Left (Enemy 4) - stop at nub
                {sx - 445 / 2, 550, sx - 445, sx}                          // Floor 4 Right (Enemy 4) - stop at nub
            };
            case 8:  return new double[][]{
                {(sw / 2 - 35) / 2, 30, 0, sw / 2 - 35},                   // Ground Left (Enemy 1) - side of pillar
                {sw - (170 / 2), 30, sw / 2 + 85, sw},                     // Ground Right (Enemy 1) - side of pillar
                {sw / 2 - 180, 151 + 30, sw / 2 - 365, sw / 2},            // Floor 2 Left (Enemy 2) - shared mid
                {sw / 2 + 180, 151 + 30, sw / 2, sw / 2 + 365},            // Floor 2 Right (Enemy 2) - shared mid
                {115 + 205, 330 + 30, 115, 115 + 410},                     // Floor 3 Left (Enemy 3)
                {sw - 525 + 205, 330 + 30, sw - 525, sw - 525 + 410},      // Floor 3 Right (Enemy 3)
                {sw / 2, 480 + 30, sw / 2 - 205, sw / 2 + 205}             // Floor 4 Mid (Enemy 4)
            };
            case 9:  return new double[][]{
                {sx / 4, 30, 0, sx / 2},                                   // Ground Left (Enemy 3)
                {sx / 2 + sx / 4, 30, sx / 2, sx},                         // Ground Right (Enemy 3)
                {sx / 2 - 145, 180 + 30, sx / 2 - 290, sx / 2},            // Floor 2 Left (Enemy 1) - stop at nub
                {sx / 2 + 145, 180 + 30, sx / 2, sx / 2 + 290},            // Floor 2 Right (Enemy 1) - stop at nub
                {150 + 190, 510 + 30, 150, 150 + 380},                     // Floor 4 Left (Enemy 4) - T top
                {sx - 530 + 190, 510 + 30, sx - 530, sx - 530 + 380}       // Floor 4 Right (Enemy 4) - T top
            };
            case 10: return new double[][]{
                {115, 30, 10, sw / 2 - 10 },                                                        // Ground Left (Gap 1: 0-230)
                {sx - 65, 30, sx /2 + 10 , sx - 10},                                          // Ground Right (Gap 3: sx-130 to sx)
                {237, 180 + 30, 95, 380},                                                  // Lower Left Wing (Stay between block and circle)
                {sx - 237, 180 + 30, sx - 380, sx - 95},                                   // Lower Right Wing (Stay between circle and block)
                {470 + 290, 320 + 30, 475, 1045},                                          // Lower Center [470, 1050]
                {237, 470 + 30, 95, 380},                                                  // Upper Left Wing (Stay between block and circle)
                {sx - 237, 470 + 30, sx - 380, sx - 95},                                   // Upper Right Wing (Stay between circle and block)
                {470 + 305, 600 + 30, 475, 1075}                                           // Top Center [470, 1080]
            };
            case 11: return new double[][]{
                {sx / 4, 30, 0, sx / 2},                                   // Ground Left (Enemy 4)
                {sx / 2 + sx / 4, 30, sx / 2, sx},                         // Ground Right (Enemy 4)
                {sx / 2 - 160, 190 + 30, sx / 2 - 325, sx / 2},            // Floor 2 Mid Left (Enemy 2) - between pillars
                {sx / 2 + 160, 190 + 30, sx / 2, sx / 2 + 325},            // Floor 2 Mid Right (Enemy 2) - between pillars
                {sx / 2 - 270, 330 + 30, sx / 2 - 475, sx / 2 - 65},       // Floor 3 Left Upper (Enemy 1)
                {sx / 2 + 270, 330 + 30, sx / 2 + 65, sx / 2 + 475},       // Floor 3 Right Upper (Enemy 1)
                {sx / 2, 510 + 30, sx / 2 - 300, sx / 2 + 350}             // Floor 4 High Moving (Enemy 5)
            };
            case 12: return new double[][]{
                {sx / 4, 30, 0, sx / 2},                                   // Ground Left (Enemy 2)
                {sx / 2 + sx / 4, 30, sx / 2, sx},                         // Ground Right (Enemy 2)
                {360 + 340, 160 + 30, 360, 260 + 780},                     // Floor 1 Main (Enemy 5) - stop at nub
                {sx - 1040 + 340, 300 + 30, sx - 1040, sx - 360},          // Floor 2 Main (Enemy 3) - stop at nub
                {355 + 340, 440 + 30, 355, 255 + 780},                     // Floor 3 Main (Enemy 5) - stop at nub
                {sx - 1040 + 340, 580 + 30, sx - 1040, sx - 360}           // Floor 4 Main (Enemy 4) - stop at nub
            };
            case 13: return new double[][]{
                {sx / 4, 30, 0, sx / 2},                                   // Ground Left (Enemy 1)
                {sx / 2 + sx / 4, 30, sx / 2, sx},                         // Ground Right (Enemy 1)
                {sx / 2 + 25, 120 + 30, sx / 2 - 280, sx / 2 + 330},       // Floor 1 Wide Center (Enemy 3)
                {285, 360 + 30, 50, 520},                                  // Floor 4 Left (Enemy 1) - avoid outer wall
                {sx - 285, 360 + 30, sx - 520, sx - 50},                   // Floor 4 Right (Enemy 1) - avoid outer wall
                {197, 490 + 30, 0, 395},                                   // Floor 5 Left (Enemy 3) - avoid inner wall
                {sx - 197, 490 + 30, sx - 395, sx}                         // Floor 5 Right (Enemy 3) - avoid inner wall
            };
            case 14: return new double[][]{
                {sx / 4, 30, 0, sx / 2},                                   // Ground Left (Enemy 3)
                {sx / 2 + sx / 4, 30, sx / 2, sx},                         // Ground Right (Enemy 3)
                {sx / 2, 120 + 30, sx / 2 - 130, sx / 2 + 130},            // Floor 1 Mid (Enemy 2)
                {455 + 125, 270 + 30, 455, 705},                           // Floor 2 Left (Enemy 1) - Inside Pillar
                {sx - 705 + 125, 270 + 30, sx - 705, sx - 455},            // Floor 2 Right (Enemy 1) - Inside Pillar
                {300 + 475, 440 + 30, 300, 300 + 950}                      // Floor 3 Bridge (Enemy 2)
            };
            case 15: return new double[][]{
                {sx / 4, 30, 0, sx / 2},                                   // Ground Left (Enemy 1)
                {sx / 2 + sx / 4, 30, sx / 2, sx},                         // Ground Right (Enemy 1)
                {365 + 160, 100 + 30, 365, 365 + 320},                     // Floor 1 Mid-left main (Enemy 3)
                {sx - 585 + 160, 100 + 30, sx - 585, sx - 265},            // Floor 1 Mid-right main (Enemy 3)
                {sx / 2, 240 + 30, sx / 2 - 205, sx / 2 + 210},            // Floor 2 Center floating moving (Enemy 4)
                {260 + 235, 390 + 30, 260, 260 + 470},                     // Floor 3 Bridge left moving (Enemy 2)
                {sx - 730 + 235, 390 + 30, sx - 730, sx - 260}             // Floor 3 Bridge right moving (Enemy 2)
            };
            case 16: return new double[][]{
                {sx / 4, 30, 0, sx / 2},                                   // Ground Left (Enemy 2)
                {sx / 2 + sx / 4, 30, sx / 2, sx},                         // Ground Right (Enemy 2)
                {100 + 182, 270 + 30, 100, 465},                           // Floor 2 Left step (Enemy 3)
                {sx - 465 + 182, 270 + 30, sx - 465, sx - 100},            // Floor 2 Right step (Enemy 3)
                {sx / 2 - 142, 400 + 30, sx / 2 - 335, sx / 2 + 50},       // Floor 3 Bridge Left (Enemy 4)
                {sx / 2 + 242, 400 + 30, sx / 2 + 50, sx / 2 + 435},       // Floor 3 Bridge Right (Enemy 4)
                {100 + 182, 560 + 30, 100, 465},                           // Floor 4 Left step (Enemy 1)
                {sx - 465 + 182, 560 + 30, sx - 465, sx - 100}             // Floor 4 Right step (Enemy 1)
            };
            case 18: return new double[][]{ {sx/2, 40, sx/4 + 100, sx/4 + 100 + 650} };   // Desert Boss restricted to ground platform
            case 19: return new double[][]{ {sx/2, 90, 200, sx - 200} };                 // Forest Boss restricted between 1st floor wing boundaries
            default: return new double[][]{{sx*0.50, 200}};
        }
    }

    public static List<Platform> getLevel(int level, int sw, int sh) {
        List<Platform> platforms = new ArrayList<>();
        double sx = sw, sy = sh;

        // Ground Floor for all levels
        platforms.add(new Platform(0, 0, sw, 30));

        switch (level) {
            //  City Levels 1-4
                case 1:
                    // Ground Floor Enemy_1_City , Enemy_4_City
                    // FLOOR 1 (Lowest)
                    platforms.add(new Platform((int)(sx - 1150), 160, 750, 20, true));          // Long mid base Enemy_1_City
                    // FLOOR 2 (Middle)
                    platforms.add(new Platform((int)(sx - 230), 250, 230, 20));                // Right middle
                    platforms.add(new Platform(0, 250, 280, 20));                               // Left middle
                    // FLOOR 3 (Upper)
                    platforms.add(new Platform(280, 400, 350, 20));                             // Left upper Enemy_4_City
                    platforms.add(new Platform((int)(sx - 600), 400, 350, 20));                // Right upper Enemy_4_City
                    break;

                case 2:
                    // Ground Floor Enemy_4_City , Enemy_4_City
                    // FLOOR 1 (Lowest)
                    platforms.add(new Platform(0, 120, 200, 20));                                // Left floor
                    platforms.add(new Platform((int)(sx - 1000), 100, 450, 20, true));          // Mid floor (moving) Enemy_1_City
                    platforms.add(new Platform((int)(sx - 200), 120, 200, 20));                // Right floor
                    // FLOOR 2 (Middle)
                    platforms.add(new Platform(330, 250, 380, 20));                             // Left middle Enemy_3_City
                    platforms.add(new Platform((int)(sx - 700), 250, 330, 20));                // Right middle Enemy_3_City
                    // FLOOR 3 (Top)
                    platforms.add(new Platform((int)(sx - 1000), 400, 450, 20, true));          // Top mid platform Enemy_1_City
                    break;

                case 3:
                    // Ground Floor Enemy_4_City , Enemy_4_City
                    // FLOOR 1 (Base)
                    platforms.add(new Platform(180, 140, 350, 20));                             // Left base  Enemy_1_City
                    platforms.add(new Platform((int)(sx - 530), 140, 350, 20));                // Right base     Enemy_3_City
                    // FLOOR 2 (Lower middle)
                    platforms.add(new Platform(0, 220, 130, 20));                               // Far left
                    platforms.add(new Platform((int)(sx - 1020), 270, 500, 20, true));          // Wide moving mid  Enemy_2_City
                    platforms.add(new Platform((int)(sx - 130), 220, 130, 20));                // Far right
                    // FLOOR 3 (Middle arch)
                    platforms.add(new Platform(120, 360, 230, 20));                             // Left arch
                    platforms.add(new Platform((int)(sx - 320), 360, 230, 20));                // Right arch
                    // FLOOR 4 (Upper arch)
                    platforms.add(new Platform(320, 500, 230, 20));                             // Left upper arch Enemy_3_City
                    platforms.add(new Platform((int)(sx - 520), 500, 230, 20));                // Right upper arch    Enemy_1_City
                    // FLOOR 5 (Peak)
                    platforms.add(new Platform((int)(sx / 2 - 150), 640, 300, 20, true));       // Top moving platform
                    break;

                case 4:
                    // Ground Floor Enemy_1_City , Enemy_4_City , Enemy_1_City
                    // FLOOR 1
                    platforms.add(new Platform(0, 130, 270, 20));                               // Left floor
                    platforms.add(new Platform((int)(sx - 270), 130, 270, 20));                // Right floor
                    // FLOOR 2
                    platforms.add(new Platform((int)(sx / 2 - 350), 200, 700, 20, true));       // Wide moving mid Enemy_2_City
                    // FLOOR 3
                    platforms.add(new Platform(0, 330, 500, 20));                               // Left wide  Enemy_3_City
                    platforms.add(new Platform(420, 330, 80, 70));                              // Left attachment (inner)
                    platforms.add(new Platform((int)(sx - 500), 330, 500, 20));                // Right wide Enemy_3_City
                    platforms.add(new Platform((int)(sx - 500), 330, 80, 70));                 // Right attachment (inner)
                    // FLOOR 4
                    platforms.add(new Platform((int)(sx / 2 - 250), 550, 500, 20, true));       // Top moving main Enemy_2_City
                    platforms.add(new Platform((int)(sx / 2 - 250), 500, 80, 40));              // Left end underneath
                    platforms.add(new Platform((int)(sx / 2 + 170), 500, 80, 40));              // Right end underneath
                    break;

                // Desert levels 5-8
                case 5:
                    // Ground Floor  Enemy_1_Desert , Enemy_1_Desert bw the two blocks
                    // FLOOR 1 (Lowest)
                    platforms.add(new Platform(250, 40, 100, 40));                              // Left small
                    platforms.add(new Platform((int)(sw - 350), 40, 100, 40));                 // Right small
                    // FLOOR 2
                    platforms.add(new Platform(0, 160, 150, 20));                               // Left step
                    platforms.add(new Platform((int)(sw / 2 - 450), 230, 870, 20, true));       // Very long moving Enemy_2_Desert
                    platforms.add(new Platform((int)(sw - 130), 160, 150, 20));                // Right step
                    // FLOOR 3
                    platforms.add(new Platform((int)(sw / 2 - 250), 370, 480, 20, true));       // Moving mid Enemy_2_Desert
                    // FLOOR 4
                    platforms.add(new Platform(0, 470, 420, 20));                               // Left long Enemy_5_Desert
                    platforms.add(new Platform(390, 500, 30, 20));                              // Left inner nub
                    platforms.add(new Platform((int)(sw - 420), 470, 420, 20));                // Right long Enemy_5_Desert
                    platforms.add(new Platform((int)(sw - 420), 500, 30, 20));                 // Right inner nub
                    // FLOOR 5 (Top)
                    platforms.add(new Platform((int)(sw / 2 - 230), 620, 180, 20));             // Left top
                    platforms.add(new Platform((int)(sw / 2 + 80), 620, 180, 20));              // Right top
                    break;

                case 6:
                    // Ground Floor Enemy_5_Desert , Enemy_5_Desert
                    // FLOOR 1 (Lowest)
                    platforms.add(new Platform(180, 120, 460, 20));                             // Left wide
                    platforms.add(new Platform((int)(sw - 640), 120, 460, 20));                // Right wide
                    // FLOOR 2
                    platforms.add(new Platform(0, 260, 285, 20));                               // Left outer Enemy_2_Desert
                    platforms.add(new Platform(390, 260, 100, 20));                             // Left inner
                    platforms.add(new Platform((int)(sw - 490), 260, 100, 20));                // Right inner
                    platforms.add(new Platform((int)(sw - 285), 260, 285, 20));                // Right outer Enemy_2_Desert
                    // FLOOR 3
                    platforms.add(new Platform(630, 265, 260, 20));                             // Center small
                    // Floor 4
                    platforms.add(new Platform(580, 395, 360, 20));        // Mid Enemy_5_Desert
                    // FLOOR 5
                    platforms.add(new Platform(0, 480, 490, 20));                               // Left main Enemy_1_Desert
                    platforms.add(new Platform(440, 290, 50, 180));                             // Left vertical wall
                    platforms.add(new Platform((int)(sw - 490), 480, 490, 20));                // Right main Enemy_1_Desert
                    platforms.add(new Platform((int)(sw - 490), 290, 50, 180));                // Right vertical wall
                    // FLOOR 6 (Peak)
                    platforms.add(new Platform((int)(sw / 2 - 330), 650, 660, 40, true));       // Top moving wide Enemy_3_Desert
                    break;

                case 7:
                    // Ground Floor Enemy_2_Desert , Enemy_2_Desert
                    // FLOOR 1
                    platforms.add(new Platform((int)(sw / 2 - 200), 130, 400, 30, true));       // Moving center
                    // FLOOR 2
                    platforms.add(new Platform(0, 230, 470, 30));                               // Left long  Enemy_3_Desert
                    platforms.add(new Platform(445, 270, 25, 30));                              // Left upward nub
                    platforms.add(new Platform((int)(sw - 470), 230, 470, 30));                // Right long     Enemy_3_Desert
                    platforms.add(new Platform((int)(sw - 470), 270, 25, 30));                 // Right upward nub
                    // FLOOR 3
                    platforms.add(new Platform((int)(sw / 2 - 200), 380, 400, 30, true));       // Moving center Enemy_5_Desert
                    // FLOOR 4
                    platforms.add(new Platform(0, 520, 470, 30));                               // Left long Enemy_4_Desert
                    platforms.add(new Platform(445, 560, 25, 30));                              // Left upward nub
                    platforms.add(new Platform((int)(sw - 470), 520, 470, 30));                // Right long Enemy_4_Desert
                    platforms.add(new Platform((int)(sw - 470), 560, 25, 30));                 // Right upward nub
                    break;

                case 8:
                    // Ground Floor Enemy_1_Desert , Enemy_1_Desert on each side of pillar of T shape
                    // FLOOR 1
                    platforms.add(new Platform((int)(sw / 2 - 35), 0, 120, 140));                // Center column
                    platforms.add(new Platform(0, 130, 230, 30));                               // Left side
                    platforms.add(new Platform((int)(sw - 170), 130, 230, 30));                // Right side
                    // FLOOR 2
                    platforms.add(new Platform((int)(sw / 2 - 365), 151, 730, 30, true));       // Very long moving  Enemy_2_Desert , Enemy_2_Desert
                    // FLOOR 3
                    platforms.add(new Platform(115, 330, 410, 30));                             // Left wide Enemy_3_Desert
                    platforms.add(new Platform((int)(sw - 525), 330, 410, 30));                // Right wide    Enemy_3_Desert
                    // FLOOR 4
                    platforms.add(new Platform((int)(sw / 2 - 205), 480, 410, 30, true));       // Moving mid Enemy_4_Desert
                    // FLOOR 5 (Highest)
                    platforms.add(new Platform(0, 610, 305, 30));                               // Left top
                    platforms.add(new Platform((int)(sw - 305), 610, 305, 30));                // Right top
                    break;

                // Forest levels 9-12
                case 9:
                    // Ground Floor Enemy_3_Forest , Enemy_3_Forest
                    // FLOOR 1
                    platforms.add(new Platform(0, 120, 180, 20));                           // Left outer
                    platforms.add(new Platform((int)(sw - 180), 120, 180, 20));                // Right outer
                    // FLOOR 2
                    platforms.add(new Platform((int)(sw / 2 - 350), 180, 700, 30));             // Main long Enemy_1_Forest , Enemy_1_Forest
                    platforms.add(new Platform((int)(sw / 2 - 350), 220, 60, 60));              // Left upward nub
                    platforms.add(new Platform((int)(sw / 2 + 290), 220, 60, 60));              // Right upward nub
                    // FLOOR 3
                    platforms.add(new Platform(0, 250, 280, 30));                           // Left side
                    platforms.add(new Platform((int)(sw - 280), 250, 280, 30));                // Right side
                    // Additional FLOOR 3 (duplicate numbering in original, but kept as is)
                    platforms.add(new Platform(0, 380, 180, 30));                               // Left outer
                    platforms.add(new Platform((int)(sw / 2 - 190), 335, 330, 30, true));   // Center small moving
                    platforms.add(new Platform((int)(sw - 180), 380, 180, 30));                // Right outer
                    // FLOOR 4
                    platforms.add(new Platform(150, 530, 380, 30));                             // Left T top Enemy_4_Forest
                    platforms.add(new Platform(315, 390, 55, 120));                             // Left T stem (downward)
                    platforms.add(new Platform((int)(sw - 530), 530, 380, 30));                // Right T top Enemy_4_Forest
                    platforms.add(new Platform((int)(sw - 370), 390, 55, 120));                // Right T stem (downward)
                    break;

            case 10:
            // Ground Floor Enemy_1_Forest , Enemy_1_Forest bw the two floor small paltforms
            // FLOOR 1 (Bottom Circles)
            platforms.add(new Platform(230, 0, 140, 40));                                // Bottom left circle
            platforms.add(new Platform((int)(sx - 270), 0, 140, 40));                    // Bottom right circle

            // FLOOR 2 (Lower Level Wings)
            platforms.add(new Platform(0, 180, 420, 30));                                // Lower left wing  Enemy_4_Forest
            platforms.add(new Platform(0, 230, 90, 40));                                 // Left block on wing
            platforms.add(new Platform(385, 220, 35, 15));                               // Left wing circle

            platforms.add(new Platform((int)(sx - 420), 180, 420, 30));                  // Lower right wing  Enemy_4_Forest
            platforms.add(new Platform((int)(sx - 90), 230, 90, 40));                   // Right block on wing
            platforms.add(new Platform((int)(sx - 420), 220, 35, 15));                  // Right wing circle

            // FLOOR 3 (Lower Center)
            platforms.add(new Platform(470, 320, 580, 30));                              // Lower floating center

            // FLOOR 4 (Upper Level Wings)
            platforms.add(new Platform(0, 470, 420, 30));                                // Upper left wing Enemy_3_Forest
            platforms.add(new Platform(0, 520, 90, 40));                                 // Left block on wing
            platforms.add(new Platform(385, 510, 35, 15));                               // Left wing circle

            platforms.add(new Platform((int)(sx - 420), 470, 420, 30));                  // Upper right wing Enemy_3_Forest
            platforms.add(new Platform((int)(sx - 90), 520, 90, 40));                   // Right block on wing
            platforms.add(new Platform((int)(sx - 420), 510, 35, 15));                  // Right wing circle

            // FLOOR 5 (Highest Center)
            platforms.add(new Platform(470, 600, 610, 30));                              // Top floating center Enemy_2_Forest

            break;

                case 11:
                    // Ground Floor Enemy_4_Forest , Enemy_4_Forest
                    // FLOOR 1
                    platforms.add(new Platform(0, 120, 120, 30));                               // Left side step
                    platforms.add(new Platform((int)(sw - 120), 120, 120, 30));                // Right side step
                    // FLOOR 2
                    platforms.add(new Platform(0, 270, 120, 30));                               // Left side step
                    platforms.add(new Platform((int)(sw / 2 - 380), 190, 760, 30));             // Main wide Enemy_2_Forest , Enemy_2_Forest
                    platforms.add(new Platform((int)(sw / 2 - 380), 190, 55, 140));             // Left support pillar
                    platforms.add(new Platform((int)(sw / 2 + 325), 190, 55, 140));             // Right support pillar
                    platforms.add(new Platform((int)(sw - 120), 270, 120, 30));                // Right side step
                    // FLOOR 3
                    platforms.add(new Platform(0, 390, 120, 30));                               // Left side step
                    platforms.add(new Platform((int)(sw / 2 - 475), 330, 410, 30));             // Left upper wide Enemy_1_Forest
                    platforms.add(new Platform((int)(sw / 2 + 65), 330, 410, 30));              // Right upper wide Enemy_1_Forest
                    platforms.add(new Platform((int)(sw - 120), 390, 120, 30));                // Right side step
                    // FLOOR 4
                    platforms.add(new Platform(0, 530, 120, 30));                               // Left side step
                    platforms.add(new Platform((int)(sw / 2 - 300), 510, 650, 30, true));       // High moving wide Enemy_5_Forest
                    platforms.add(new Platform((int)(sw - 120), 530, 120, 30));                // Right side step
                    break;

                case 12:
                    // Ground Floor Enemy_2_Forest , Enemy_2_Forest
                    // SIDE STEPS
                    platforms.add(new Platform(0, 120, 85, 20));                                // Left step F1
                    platforms.add(new Platform((int)(sw - 85), 120, 85, 20));                  // Right step F1
                    platforms.add(new Platform(0, 290, 85, 20));                                // Left step F2
                    platforms.add(new Platform((int)(sw - 85), 290, 85, 20));                  // Right step F2
                    platforms.add(new Platform(0, 460, 85, 20));                                // Left step F3
                    platforms.add(new Platform((int)(sw - 85), 460, 85, 20));                  // Right step F3
                    platforms.add(new Platform(0, 630, 85, 20));                                // Left step F4
                    platforms.add(new Platform((int)(sw - 85), 630, 85, 20));                  // Right step F4
                    // MAIN PLATFORMS
                    platforms.add(new Platform(260, 160, 780, 30));                             // Floor 1 main (left-aligned) Enemy_5_Forest
                    platforms.add(new Platform(260, 220, 100, 50));                             // Floor 1 left nub
                    platforms.add(new Platform((int)(sw - 1040), 300, 780, 30));               // Floor 2 main (right-aligned) Enemy_3_Forest
                    platforms.add(new Platform((int)(sw - 360), 340, 100, 50));                // Floor 2 right nub
                    platforms.add(new Platform(255, 440, 780, 30));                             // Floor 3 main (left-aligned)Enemy_5_Forest
                    platforms.add(new Platform(255, 500, 100, 50));                             // Floor 3 left nub
                    platforms.add(new Platform((int)(sw - 1040), 580, 780, 30));               // Floor 4 main (right-aligned) Enemy_4_Forest
                    platforms.add(new Platform((int)(sw - 360), 620, 100, 50));                // Floor 4 right nub
                    break;

                //Volcano levels 13-16
                case 13:
                    // Ground Floor Enemy_1_Volcano
                    // FLOOR 1
                    platforms.add(new Platform((int)(sw / 2 - 280), 120, 610, 30));             // Wide center Enemy_3_Volcano
                    // FLOOR 2
                    platforms.add(new Platform(0, 150, 250, 30));                               // Left side
                    platforms.add(new Platform((int)(sw - 250), 120, 250, 30));                // Right side
                    // FLOOR 3
                    platforms.add(new Platform((int)(sw / 2 - 165), 275, 330, 30, true));       // Moving center
                    // FLOOR 4
                    platforms.add(new Platform(0, 360, 520, 30));                               // Left main Enemy_1_Volcano
                    platforms.add(new Platform(0, 400, 50, 80));                                // Left outer wall
                    platforms.add(new Platform((int)(sw - 520), 360, 520, 30));                // Right main Enemy_1_Volcano
                    platforms.add(new Platform((int)(sw - 50), 400, 50, 80));                  // Right outer wall
                    // FLOOR 5
                    platforms.add(new Platform(0, 490, 450, 30));                               // Left main Enemy_3_Volcano
                    platforms.add(new Platform(395, 530, 55, 80));                              // Left inner wall
                    platforms.add(new Platform((int)(sw - 450), 490, 450, 30));                // Right main Enemy_3_Volcano
                    platforms.add(new Platform((int)(sw - 450), 530, 55, 80));                 // Right inner wall
                    // FLOOR 6 (Highest)
                    platforms.add(new Platform(240, 620, 210, 30));                             // Left hook
                    platforms.add(new Platform((int)(sw - 450), 620, 210, 30));                // Right hook
                    break;

                case 14:
                    // Ground Floor Enemy_3_Volcano , Enemy_3_Volcano
                    // SIDE STEPS
                    platforms.add(new Platform(0, 150, 95, 30));                                // Left step F1 (+50)
                    platforms.add(new Platform((int)(sw - 95), 150, 95, 30));                  // Right step F1 (+50)
                    platforms.add(new Platform(0, 320, 95, 30));                                // Left step F2 (+50)
                    platforms.add(new Platform((int)(sw - 95), 320, 95, 30));                  // Right step F2 (+50)
                    platforms.add(new Platform(0, 490, 95, 30));                                // Left step F3 (+50)
                    platforms.add(new Platform((int)(sw - 95), 490, 95, 30));                  // Right step F3 (+50)
                    // CENTRAL STRUCTURE
                    platforms.add(new Platform((int)(sw / 2 - 130), 170, 260, 30));             // Lowest mid (+50)
                    platforms.add(new Platform(195, 320, 510, 30));                         // Left split (+50)
                    platforms.add(new Platform((int)(sw - 705), 320, 510, 30));                // Right split (+50)
                    platforms.add(new Platform(410, 360, 45, 120));                         // Left pillar (+50)
                    platforms.add(new Platform((int)(sw - 455), 360, 45, 120));                // Right pillar (+50)
                    // FLOOR 3 (Highest mid)
                    platforms.add(new Platform(300, 490, 950, 30, true));                       // Long bridge moving (+50)
                    break;

                case 15:
                    // Ground Floor Enemy_1_Volcano , Enemy_1_Volcano
                    // FLOOR 1
                    platforms.add(new Platform(0, 150, 170, 30));                           // Left small step
                    platforms.add(new Platform(365, 150, 320, 30));                         // Mid-left main  Enemy_3_Volcano
                    platforms.add(new Platform((int)(sw - 585), 150, 320, 30));                // Mid-right main Enemy_3_Volcano
                    platforms.add(new Platform((int)(sw - 70), 150, 170, 30));                 // Right small step
                    // FLOOR 2
                    platforms.add(new Platform(90, 270, 215, 30));                              // Lower left mid
                    platforms.add(new Platform((int)(sw / 2 - 205), 290, 415, 30, true));       // Center floating small moving Enemy_4_Volcano
                    platforms.add(new Platform((int)(sw - 305), 270, 175, 30));                // Lower right mid
                    // FLOOR 3 (Bridge & pillars)
                    platforms.add(new Platform(260, 310, 45, 120));                             // Left pillar
                    platforms.add(new Platform((int)(sw - 305), 310, 45, 120));                // Right pillar
                    platforms.add(new Platform(0, 390, 175, 30));                               // Far left side step
                    platforms.add(new Platform(260, 440, 470, 30, true));                       // Bridge left (moving) Enemy_2_Volcano
                    platforms.add(new Platform(sw - 730, 440, 470, 30, true));                 // Bridge right (moving) Enemy_2_Volcano
                    platforms.add(new Platform((int)(sw - 175), 390, 175, 30));                // Far right side step
                    // FLOOR 4 (Highest)
                    platforms.add(new Platform(110, 610, 320, 30));                             // Top left floating
                    platforms.add(new Platform((int)(sw - 430), 610, 320, 30));                // Top right floating
                    break;

                case 16:
                    // Ground Floor  Enemy_2_Volcano , Enemy_2_Volcano
                    // FLOOR 1 (Lowest)
                    platforms.add(new Platform(170, 130, 315, 30));                             // Left step
                    platforms.add(new Platform((int)(sw / 2 - 125), 0, 250, 140));              // Center large block
                    platforms.add(new Platform((int)(sw - 485), 130, 315, 30));                // Right step
                    // FLOOR 2 (+170 gap)
                    platforms.add(new Platform(100, 270, 365, 30));                             // Left step Enemy_3_Volcano
                    platforms.add(new Platform((int)(sw - 465), 270, 365, 30));                // Right step Enemy_3_Volcano
                    // FLOOR 3
                    platforms.add(new Platform((int)(sw / 2 - 335), 400, 770, 30, true));       // Center long bridge moving Enemy_4_Volcano , Enemy_4_Volcano
                    // FLOOR 4
                    platforms.add(new Platform(100, 560, 365, 30));                             // Left step Enemy_1_Volcano
                    platforms.add(new Platform((int)(sw - 465), 560, 365, 30));                // Right step Enemy_1_Volcano
                    // FLOOR 5
                    platforms.add(new Platform(0, 730, 215, 30));                               // Far left step
                    platforms.add(new Platform((int)(sw - 215), 730, 215, 30));                // Far right step
                    break;

                // City Boss level
                case 17:
                    // FLOOR 1 (Lowest - Bottom)
                    platforms.add(new Platform(100, 100, 650, 30));                              // Bottom staggered platform

                    // FLOOR 2 (Lower Middle)
                    platforms.add(new Platform(0, 250, 650, 30));                                // Left-aligned platform

                    // FLOOR 3 (Middle)
                    platforms.add(new Platform((int)(sx - 350), 375, 350, 70));                  // Isolated thick platform on right (+50)

                    // FLOOR 4 (Upper Middle)
                    platforms.add(new Platform(100, 400, 650, 30));                              // Mid-upper staggered platform

                    // FLOOR 5 (Highest - Top)
                    platforms.add(new Platform(0, 550, 650, 30));                                // Top-left platform
                    break;

                // Desert Boss level
                case 18:
                    // FLOOR 1 (Lowest - Center)
                    platforms.add(new Platform(sw/4+100, 0, 650, 40));                               // Wide base platform

                    // FLOOR 2 (Inner Wings)
                    platforms.add(new Platform(220, 150, 200, 20));                              // Lower inner left
                    platforms.add(new Platform((int)(sx - 420), 150, 200, 20));                  // Lower inner right

                    // FLOOR 3 (Outer Wings)
                    platforms.add(new Platform(80, 280, 200, 20));                               // Mid outer left
                    platforms.add(new Platform((int)(sx - 280), 280, 200, 20));                  // Mid outer right

                    // FLOOR 4 (Top Edges)
                    platforms.add(new Platform(0, 420, 200, 20));                                // High far left
                    platforms.add(new Platform((int)(sx - 200), 420, 200, 20));                  // High far right

                    // FLOOR 5 (Highest - Center)
                    platforms.add(new Platform(sw/4 , 550, sw/2, 30));                           // Final top center goal
                    break;

                // case 19
                // Forest Boss level

            case 19:

            // FLOOR 1 (Lowest - Ground Base)
            platforms.add(new Platform(0, 0, (int)(sx), 90));                     // Main wide ground floor

            // FLOOR 2 (Lower Tier)
            platforms.add(new Platform(0, 200, 200, 30));                               // Far left
            platforms.add(new Platform((int)(sx - 200), 200, 200, 30));                  // Far right

            // FLOOR 3 (Middle Tier)
            platforms.add(new Platform(130, 350, 210, 30));                              // Mid left
            platforms.add(new Platform((int)(sx - 340), 350, 210, 30));                  // Mid right

            // FLOOR 4 (Highest Tier - Top Pyramid)
            platforms.add(new Platform(250, 500, 190, 30));                               // Top left
            platforms.add(new Platform((int)(sx/2 - 210), 600, 400, 30));                // Top center (under red circle)
            platforms.add(new Platform((int)(sx - 440), 500, 190, 30));                   // Top right

                break;

            case 20:

                // FLOOR 1 (Lowest)
                platforms.add(new Platform(660, 100, 630, 25));                               // Bottom staggered platform

                // FLOOR 2 (Lower Middle)
                platforms.add(new Platform(sw - 730 , 260, 630, 25));                         // Anchored further right

                // FLOOR 3 (Middle)
                platforms.add(new Platform((int)(0), 325, 350, 70));                  // Isolated thick platform on right

                // FLOOR 4 (Upper Middle)
                platforms.add(new Platform(660, 420, 630, 25));                              // Top anchored right

                // FLOOR 5 (Highest)
                platforms.add(new Platform(sw - 630 , 580, 630, 25));                              // Middle staggered platform

                break;

                default:
                    // Fallback: simple platforms
                    platforms.add(new Platform((int)(sw / 2 - 150), 150, 300, 20, true));
                    platforms.add(new Platform(0, 300, 200, 20));
                    platforms.add(new Platform((int)(sw - 200), 300, 200, 20));
                    break;
            }

        for (Platform p : platforms) p.setScreenHeight(sh);
        return platforms;
    }
}
