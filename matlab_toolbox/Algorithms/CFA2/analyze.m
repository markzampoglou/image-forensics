function [F1Map,CFADetected, F1] = analyze( imPath )
    % Copyright (C) 2016 Markos Zampoglou
    % Information Technologies Institute, Centre for Research and Technology Hellas
    % 6th Km Harilaou-Thermis, Thessaloniki 57001, Greece
    
    im=CleanUpImage(imPath);
    [F1Map,CFADetected, F1] = CFATamperDetection_F1(im);
end