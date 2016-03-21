function [F2Map,CFADetected] = analyze( imPath )
    % Copyright (C) 2016 Markos Zampoglou
    % Information Technologies Institute, Centre for Research and Technology Hellas
    % 6th Km Harilaou-Thermis, Thessaloniki 57001, Greece
    
    c=parcluster('local');
    % adjust the number of workers to the CPU capabilities.
    c.NumWorkers=4;
    parpool(c,c.NumWorkers);
    
    im=CleanUpImage(imPath);
    [F2Map,CFADetected] = CFATamperDetection_F2(im);
    
    delete(gcp)
end

