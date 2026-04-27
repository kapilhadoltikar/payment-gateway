import os
import csv
import glob

def calculate_coverage(root_dir):
    total_instructions = 0
    covered_instructions = 0
    
    print(f"{'Module':<30} | {'Coverage':<10} | {'Instructions':<15}")
    print("-" * 65)

    xml_files = glob.glob(os.path.join(root_dir, '**/target/site/jacoco/jacoco.csv'), recursive=True)
    
    if not xml_files:
        print("No Jacoco CSV reports found.")
        return

    for file_path in xml_files:
        module_name = os.path.basename(os.path.dirname(os.path.dirname(os.path.dirname(os.path.dirname(file_path)))))
        # Fix path parsing if unexpected structure, but typically: module/target/site/jacoco/jacoco.csv
        # so dirname 4 times goes to module root. 
        # Actually simplest is just to take the part of path before target.
        rel_path = os.path.relpath(file_path, root_dir)
        module_name = rel_path.split(os.sep)[0]

        try:
            with open(file_path, 'r') as f:
                reader = csv.DictReader(f)
                mod_inst = 0
                mod_cov = 0
                for row in reader:
                    missed = int(row['INSTRUCTION_MISSED'])
                    covered = int(row['INSTRUCTION_COVERED'])
                    mod_inst += (missed + covered)
                    mod_cov += covered
                
                if mod_inst > 0:
                    coverage = (mod_cov / mod_inst) * 100
                    print(f"{module_name:<30} | {coverage:6.2f}%    | {mod_cov}/{mod_inst}")
                else:
                    print(f"{module_name:<30} | N/A        | 0/0")

                total_instructions += mod_inst
                covered_instructions += mod_cov
        except Exception as e:
            print(f"Error reading {file_path}: {e}")

    print("-" * 65)
    if total_instructions > 0:
        total_coverage = (covered_instructions / total_instructions) * 100
        print(f"{'TOTAL':<30} | {total_coverage:6.2f}%    | {covered_instructions}/{total_instructions}")
    else:
        print("Total coverage: N/A")

if __name__ == "__main__":
    calculate_coverage(".")
