variable "aws_region" {
  description = "AWS region to deploy the infrastructure in"
  type        = string
  default     = "eu-central-1"
}

variable "aws_profile" {
  description = "The profile to use"
  type        = string
  default     = "default"
}


variable "role_arn" {
  description = "IAM role to assume (used in CI/CD)"
  type        = string
  default     = "arn:aws:iam::471112759337:role/terraform-manual-role"
}

# VPC variables
variable "vpc_cidr_block" {
  description = "CIDR block for the VPC"
  type        = string
  default     = "10.0.0.0/16"
}

variable "public_subnet_cidrs" {
  description = "List of public subnet CIDRs"
  type        = list(string)
  default     = ["10.0.1.0/24", "10.0.2.0/24"]
}

variable "private_subnet_cidrs" {
  description = "List of private subnet CIDRs"
  type        = list(string)
  default     = ["10.0.3.0/24", "10.0.4.0/24"]
}

# EKS variables
variable "cluster_name" {
  description = "EKS cluster name"
  type        = string
  default     = "my-eks-cluster"
}

variable "cluster_version" {
  description = "EKS Kubernetes version"
  type        = string
  default     = "1.29"
}

variable "node_groups" {
  description = "Managed node group configurations"
  type = any 
  default = {
  managed_nodes = {
    desired_capacity = 3
    min_capacity     = 2
    max_capacity     = 4
    instance_types   = ["t3.medium"]
    key_name         = ""
    additional_tags  = {
      "k8s-node-role" = "managed"
      "env"           = "dev"
    }
  }

#  spot_workers = {
#    desired_capacity = 1
#    min_capacity     = 0
#    max_capacity     = 2
#    instance_types   = ["t3.small"]
#    key_name         = ""
#    additional_tags  = {
#      "k8s-node-role" = "spot"
#      "env"           = "dev"
#    }
#  }
}
}

variable "tags" {
  description = "Common tags applied to all resources"
  type        = map(string)
  default     = {
    Environment = "dev"
    Project     = "example"
  }
}