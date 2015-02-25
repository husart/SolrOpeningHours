<pre>
<?php

function opening_hours_normalize($interval) {
  $ZHH = 9;
  $DHH = 12;
  $ZDDHH = 17;

  $stored = array(
    'CLS' => array(),
    'DHH' => array(),
    'ZDDHH' => array(),
    'ZHH' => array()
  );

  $imploded = '';
  foreach($interval as $int) {
    if($int < 0) {
      $stored['CLS'][] = $int;
      continue;
    }
    switch(strlen($int)) {
      case $ZHH:
        $stored['ZHH'][] = $int;
        break;
      case $DHH:
        $stored['DHH'][] = $int;
        break;
      case $ZDDHH:
        $stored['ZDDHH'][] = $int;
        break;
    }
  }
  rsort($stored['ZDDHH']);
  rsort($stored['ZHH']);
  rsort($stored['DHH']);
  sort($stored['CLS']);

  foreach($stored as $key => $st) {
    if(empty($stored[$key])) {
     continue;
    }
    $imploded .= implode(';', $stored[$key]);
    
    if($imploded[strlen($imploded) -1] !=';') {
      $imploded .=';';
    }
  }

  return $imploded;
}

